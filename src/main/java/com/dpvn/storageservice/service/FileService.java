package com.dpvn.storageservice.service;

import com.dpvn.shared.exception.BadRequestException;
import com.dpvn.shared.service.AbstractService;
import com.dpvn.shared.util.DateUtil;
import com.dpvn.shared.util.FastMap;
import com.dpvn.shared.util.StringUtil;
import com.dpvn.storageservice.config.StorageProperties;
import com.dpvn.storageservice.domain.File;
import com.dpvn.storageservice.domain.HttpDownloadResult;
import com.dpvn.storageservice.repository.FileRepository;
import java.io.*;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.HexFormat;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService extends AbstractService {

  private final FileRepository fileRepository;
  private final StorageProperties storageProperties;
  private final FileCacheService fileCacheService;
  private final ThreadPoolTaskExecutor taskExecutor;

  public FileService(
      FileRepository fileRepository,
      StorageProperties storageProperties,
      FileCacheService fileCacheService,
      @Qualifier("storageTaskExecutor") ThreadPoolTaskExecutor taskExecutor) {
    this.fileRepository = fileRepository;
    this.storageProperties = storageProperties;
    this.fileCacheService = fileCacheService;
    this.taskExecutor = taskExecutor;
  }

  /** Upload từ multipart file */
  public File upload(MultipartFile file) {
    try (InputStream rawIn = file.getInputStream();
        BufferedInputStream in = new BufferedInputStream(rawIn)) {

      // Detect mime type (có fallback an toàn)
      String mimeType = FileUtil.getMimeType(file.getContentType(), in);

      String source =
          String.format("User upload file %s at %s", file.getOriginalFilename(), DateUtil.now());
      return storeFile(in, source, file.getOriginalFilename(), mimeType, file.getSize());
    } catch (IOException e) {
      LOGGER.error("FAILED to upload file with error = {}", e.getMessage());
      throw new BadRequestException("FAILED to upload file with error = " + e.getMessage());
    }
  }

  /** Upload từ 1 URL */
  public File uploadFromUrl(FastMap source) {
    String url = source.getString("url");
    String forceName = source.getString("forceName");
    if (StringUtil.isEmpty(url) || !url.startsWith("http")) {
      throw new BadRequestException("url [" + url + "] is not valid");
    }

    try {
      HttpDownloadResult download = FileUtil.download(url);
      InputStream in = new BufferedInputStream(download.getInputStream());
      return storeFile(
          in,
          url,
          StringUtil.isNotEmpty(forceName) ? forceName : download.getOriginalName(),
          download.getMimeType(),
          download.getSize());
    } catch (IOException e) {
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

  /** Get file metadata theo slug */
  public File getFileBySlug(String slug, boolean refreshCache) {
    if (refreshCache) {
      fileCacheService.evictMeta(slug);
    }
    File file = fileCacheService.getMeta(slug);
    if (file == null) {
      file =
          fileRepository
              .findBySlug(slug)
              .orElseThrow(() -> new RuntimeException("File " + slug + " not found"));
      fileCacheService.putMeta(file);
    }
    return file;
  }

  /** Thực sự lưu file vào storage + metadata */
  private File storeFile(
      InputStream in, String source, String originalName, String mimeType, long size)
      throws IOException {

    String ext = FileUtil.getExtensionFromMimeType(mimeType, originalName);
    String slug = FileUtil.getSlug();
    String relative = FileUtil.getRelative(slug + "." + ext);

    Path target = Paths.get(storageProperties.getBasePath()).resolve(relative);
    Files.createDirectories(target.getParent());

    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not available", e);
    }

    try (DigestInputStream dis = new DigestInputStream(in, md);
        OutputStream out =
            Files.newOutputStream(
                target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = dis.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
    }

    // MD5 sau khi copy xong
    String md5 = HexFormat.of().formatHex(md.digest());
    long fileSize = size > 0 ? size : Files.size(target);

    return saveMetadata(source, originalName, mimeType, slug, relative, fileSize, md5);
  }

  /** Lưu metadata vào DB + cache */
  private File saveMetadata(
      String source,
      String originalName,
      String mimeType,
      String slug,
      String relativePath,
      long fileSize,
      String md5) {

    File entity = new File();
    entity.setSlug(slug);
    entity.setSource(source);
    entity.setFileName(originalName);
    entity.setFilePath(relativePath);
    entity.setFileSize(fileSize);
    entity.setFileMimeType(StringUtil.isNotEmpty(mimeType) ? mimeType : "application/octet-stream");
    entity.setHash(md5);
    entity.setCreatedAt(DateUtil.now());
    entity.setUpdateAt(DateUtil.now());

    Optional<File> dbFileOpt = fileRepository.findFirstBySource(source);
    if (dbFileOpt.isPresent()) {
      return dbFileOpt.get();
    }
    // md5 ko work
    //    dbFileOpt = fileRepository.findFirstByHash(md5);
    //    if (dbFileOpt.isPresent()) {
    //      return dbFileOpt.get();
    //    }

    fileRepository.save(entity);
    fileCacheService.putMeta(entity);
    return entity;
  }
}
