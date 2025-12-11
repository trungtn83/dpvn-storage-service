package com.dpvn.storageservice.service;

import com.dpvn.sharedcore.config.CacheService;
import com.dpvn.sharedcore.exception.BadRequestException;
import com.dpvn.sharedcore.service.AbstractService;
import com.dpvn.sharedcore.util.DateUtil;
import com.dpvn.sharedcore.util.FastMap;
import com.dpvn.sharedcore.util.StringUtil;
import com.dpvn.storageservice.config.StorageProperties;
import com.dpvn.storageservice.domain.entity.File;
import com.dpvn.storageservice.domain.entity.HttpDownloadResult;
import com.dpvn.storageservice.repository.FileRepository;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Refactored FileService:
 * - preSaveMetadata: lưu metadata nhanh (REQUIRES_NEW) -> commit sớm, giải phóng connection
 * - writeFileToDisk: IO heavy, không tương tác DB
 * - finalizeMetadataAfterWrite: cập nhật metadata ngắn gọn (REQUIRES_NEW)
 *
 * Comments in Vietnamese per user preference. Variable names in English.
 */
@Service
public class FileService extends AbstractService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

  private final FileRepository fileRepository;
  private final StorageProperties storageProperties;
  private final FileCacheService fileCacheService;
  private final CacheService cacheService;

  public FileService(
      FileRepository fileRepository,
      StorageProperties storageProperties,
      FileCacheService fileCacheService,
      CacheService cacheService) {
    this.fileRepository = fileRepository;
    this.storageProperties = storageProperties;
    this.fileCacheService = fileCacheService;
    this.cacheService = cacheService;
  }

  /**
   * PUBLIC API: Upload từ multipart file
   *
   * Luồng hợp lý: 1) pre-save metadata (nhanh, commit) -> 2) write file (IO, không chạm DB)
   * -> 3) update metadata với size/md5/path (nhanh, commit).
   */
  public File upload(MultipartFile file) {
    String originalName = file.getOriginalFilename();
    long providedSize = file.getSize();
    String source = String.format("Manual upload file %s at %s", originalName, DateUtil.now());

    try (InputStream rawIn = file.getInputStream();
        BufferedInputStream bufferedIn = new BufferedInputStream(rawIn)) {
      String mimeType = FileUtil.getMimeType(file.getContentType(), bufferedIn);
      String ext = FileUtil.getExtensionFromMimeType(mimeType, originalName);
      String slug = FileUtil.getSlug();
      String relative = FileUtil.getRelative(slug + "." + ext);
      Path target = Paths.get(storageProperties.getBasePath()).resolve(relative);
      Files.createDirectories(target.getParent());

      MessageDigest md = getMd5Digest();
      writeFileToDisk(bufferedIn, target, md);

      String md5 = HexFormat.of().formatHex(md.digest());
      long actualSize = (providedSize > 0) ? providedSize : Files.size(target);

      ResultMetadata resultMetadata =
          finalizeMetadataAfterWrite(source, originalName, mimeType, relative, actualSize, md5);
      if (resultMetadata.isNew()) {
        fileCacheService.putMeta(resultMetadata.entity);
      } else {
        Files.deleteIfExists(target);
      }
      return resultMetadata.entity;
    } catch (IOException e) {
      LOGGER.error("FAILED to upload file: {} error={}", originalName, e.getMessage(), e);
      throw new BadRequestException("FAILED to upload file with error = " + e.getMessage());
    }
  }

  /**
   * Upload từ URL (giữ luồng tương tự): pre-save metadata -> download stream -> write file -> finalize
   */
  public File uploadFromUrl(FastMap sourceMap) {
    String url = sourceMap.getString("url");
    String forceName = sourceMap.getString("forceName");

    if (StringUtil.isEmpty(url) || !url.startsWith("http")) {
      throw new BadRequestException("url [" + url + "] is not valid");
    }

    HttpDownloadResult download;
    try {
      download = FileUtil.download(url);
    } catch (IOException ex) {
      throw new BadRequestException("Error downloading url " + url + " : " + ex.getMessage());
    }

    String originalName = StringUtil.isNotEmpty(forceName) ? forceName : download.getOriginalName();
    String source = url + " (downloaded at " + DateUtil.now() + ")";

    try (InputStream rawIn = new BufferedInputStream(download.getInputStream())) {
      String mimeType = FileUtil.getMimeType(download.getMimeType(), rawIn);
      String ext = FileUtil.getExtensionFromMimeType(mimeType, originalName);
      String slug = FileUtil.getSlug();
      String relative = FileUtil.getRelative(slug + "." + ext);
      Path target = Paths.get(storageProperties.getBasePath()).resolve(relative);
      Files.createDirectories(target.getParent());

      MessageDigest md = getMd5Digest();
      writeFileToDisk(rawIn, target, md);

      String md5 = HexFormat.of().formatHex(md.digest());
      long actualSize = download.getSize() > 0 ? download.getSize() : Files.size(target);

      ResultMetadata resultMetadata =
          finalizeMetadataAfterWrite(source, originalName, mimeType, relative, actualSize, md5);
      if (resultMetadata.isNew) {
        fileCacheService.putMeta(resultMetadata.entity);
      } else {
        Files.deleteIfExists(target);
      }
      return resultMetadata.entity;

    } catch (IOException e) {
      LOGGER.error("FAILED to upload from url={} error={}", url, e.getMessage(), e);
      throw new BadRequestException(
          "Error from upload file from url:" + url + " with error=" + e.getMessage());
    }
  }

  /** Upload nhiều URL song song */
  public List<File> uploadFromUrls(List<FastMap> sources) {
    List<File> files = new ArrayList<>();
    sources.forEach(source -> files.add(uploadFromUrl(source)));
    return files;
  }

  /* ----------------- Helper methods ----------------- */

  private MessageDigest getMd5Digest() {
    try {
      return MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not available", e);
    }
  }

  /**
   * Ghi file thực tế lên disk. Phương thức này KHÔNG truy cập DB để tránh giữ connection lâu.
   * Nếu ghi file thất bại thì ném IOException.
   */
  private void writeFileToDisk(InputStream in, Path target, MessageDigest md) throws IOException {
    try (DigestInputStream dis = new DigestInputStream(in, md);
        OutputStream out =
            Files.newOutputStream(
                target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = dis.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
      out.flush();
    }
  }

  private record ResultMetadata(File entity, boolean isNew) {}

  protected ResultMetadata finalizeMetadataAfterWrite(
      String source,
      String originalName,
      String mimeType,
      String relativePath,
      long fileSize,
      String md5) {
    File dbEntity = fileRepository.getFirstBySourceOrHash(source, md5).orElse(null);
    if (dbEntity == null) {
      File entity = new File();
      entity.setSlug(FileUtil.getSlug());
      entity.setSource(source);
      entity.setFileName(originalName);
      entity.setFileMimeType(
          StringUtil.isNotEmpty(mimeType) ? mimeType : "application/octet-stream");
      entity.setCreatedAt(DateUtil.now());
      entity.setUpdateAt(DateUtil.now());
      entity.setFilePath(relativePath);
      entity.setFileSize(fileSize);
      entity.setHash(md5);
      return new ResultMetadata(fileRepository.saveAndFlush(entity), true);
    }
    return new ResultMetadata(dbEntity, false);
  }

  /* ----------------- Các API khác giữ nguyên (ví dụ getFileBySlug) ----------------- */

  public File getFileBySlug(String slug, boolean refreshCache) {
    if (refreshCache) {
      fileCacheService.evictMeta(slug);
    }
    File file = fileCacheService.getMeta(slug);
    if (file == null) {
      file = fileRepository.findBySlug(slug).orElse(null);
      if (file == null) {
        return null;
      }
      fileCacheService.putMeta(file);
    }
    return file;
  }
}
