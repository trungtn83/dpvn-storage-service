package com.dpvn.storageservice.controller;

import com.dpvn.shared.domain.BeanMapper;
import com.dpvn.shared.util.FastMap;
import com.dpvn.shared.util.StringUtil;
import com.dpvn.storageservice.domain.File;
import com.dpvn.storageservice.domain.FileDto;
import com.dpvn.storageservice.service.FileService;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {
  private final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public FileDto upload(@RequestParam("file") MultipartFile file) {
    long start = System.currentTimeMillis();
    LOGGER.info(
        "[UPLOAD] Start upload file: name={}, size={} bytes, contentType={}",
        file.getOriginalFilename(),
        file.getSize(),
        file.getContentType());

    File uploadedFile = fileService.upload(file);
    FileDto fileDto = BeanMapper.instance().map(uploadedFile, FileDto.class);

    long took = System.currentTimeMillis() - start;
    LOGGER.info(
        "[UPLOAD] Success file={}, mimeType={}, size={}, took={}ms",
        fileDto.getFileName(),
        fileDto.getFileMimeType(),
        fileDto.getFileSize(),
        took);
    return fileDto;
  }

  @PostMapping("/upload-from-url")
  public FileDto uploadFromUrl(@RequestBody FastMap source) {
    long start = System.currentTimeMillis();

    String url = source.getString("url");
    String forceName =
        StringUtil.isEmpty(source.getString("forceName"))
            ? "DEFAULT"
            : source.getString("forceName");

    LOGGER.info(
        "[UPLOAD-FROM-URL] Start download file from URL={} with name is {}", url, forceName);

    File file = fileService.uploadFromUrl(source);
    FileDto fileDto = BeanMapper.instance().map(file, FileDto.class);

    long took = System.currentTimeMillis() - start;
    LOGGER.info(
        "[UPLOAD-FROM-URL] Success URL={}, name={}, mimeType={}, size={}, took={}ms",
        url,
        fileDto.getFileName(),
        fileDto.getFileMimeType(),
        fileDto.getFileSize(),
        took);

    return fileDto;
  }

  @PostMapping("/upload-from-urls")
  public List<FileDto> uploadFromUrls(@RequestBody List<FastMap> sources) {
    LOGGER.info("[UPLOAD-FROM-URLS] Start processing {} URLs", sources.size());
    long start = System.currentTimeMillis();

    List<FileDto> fileDtos =
        fileService.uploadFromUrls(sources).stream()
            .map(file -> BeanMapper.instance().map(file, FileDto.class))
            .toList();

    long took = System.currentTimeMillis() - start;
    LOGGER.info("[UPLOAD-FROM-URLS] Completed {} files in {}ms", fileDtos.size(), took);

    return fileDtos;
  }

  @GetMapping("/{slug}")
  public ResponseEntity<Void> show(
      @PathVariable String slug,
      @RequestParam(defaultValue = "inline") String disposition,
      @RequestParam(required = false, defaultValue = "false") boolean ignoreCache) {
    LOGGER.info(
        "[GET-FILE] slug={}, disposition={}, ignoreCache={}", slug, disposition, ignoreCache);

    File file = fileService.getFileBySlug(slug, ignoreCache);
    HttpHeaders headers = buildFileHeaders(file, disposition, true);

    LOGGER.info(
        "[GET-FILE] OK slug={}, mimeType={}, size={}",
        slug,
        file.getFileMimeType(),
        file.getFileSize());

    return new ResponseEntity<>(headers, HttpStatus.OK);
  }

  @RequestMapping(value = "/{slug}", method = RequestMethod.HEAD)
  public ResponseEntity<Void> head(
      @PathVariable String slug,
      @RequestParam(defaultValue = "inline") String disposition,
      @RequestParam(required = false, defaultValue = "false") boolean ignoreCache) {
    LOGGER.debug(
        "[HEAD-FILE] slug={}, disposition={}, ignoreCache={}", slug, disposition, ignoreCache);

    File file = fileService.getFileBySlug(slug, ignoreCache);
    HttpHeaders headers = buildFileHeaders(file, disposition, false);
    return new ResponseEntity<>(headers, HttpStatus.OK);
  }

  private HttpHeaders buildFileHeaders(File file, String disposition, boolean includeAccel) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(file.getFileMimeType()));

    // Nếu bạn có thể lấy được dung lượng file từ DB hoặc metadata thì thêm vào luôn
    if (file.getFileSize() != null) {
      headers.setContentLength(file.getFileSize());
    }

    // xử lý fileName để chắc chắn có đuôi
    String safeFileName = ensureExtension(file.getFileName(), file.getFileMimeType());

    // set Content-Disposition thủ công theo RFC5987
    String cd = buildContentDispositionHeader(disposition, safeFileName);
    headers.add(HttpHeaders.CONTENT_DISPOSITION, cd);

    // Chỉ GET mới cần cho nginx redirect
    if (includeAccel) {
      headers.set("X-Accel-Redirect", "/protected/" + file.getFilePath());
    }

    return headers;
  }

  private static String rfc5987Encode(String s) {
    // percent-encode bytes of UTF-8 (RFC 5987)
    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
    StringBuilder sb = new StringBuilder(bytes.length * 3);
    for (byte b : bytes) {
      int ub = b & 0xFF;
      // unreserved chars per RFC3986: ALPHA / DIGIT / "-" / "." / "_" / "~"
      if ((ub >= 'a' && ub <= 'z')
          || (ub >= 'A' && ub <= 'Z')
          || (ub >= '0' && ub <= '9')
          || ub == '-'
          || ub == '.'
          || ub == '_'
          || ub == '~') {
        sb.append((char) ub);
      } else {
        sb.append('%');
        sb.append(String.format("%02X", ub));
      }
    }
    return sb.toString();
  }

  private static String asciiFallback(String s) {
    if (s == null) return "file";
    // NFKD/NFD normalize then strip diacritics
    String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
    // remove combining diacritics
    String noDiacritics = normalized.replaceAll("\\p{M}", "");
    // remove any non-printable / non-ascii chars, replace quotes
    String safe = noDiacritics.replaceAll("[^\\x20-\\x7E]", "").replace("\"", "'").trim();
    if (safe.isEmpty()) return "file";
    return safe;
  }

  private static String buildContentDispositionHeader(String disposition, String fileName) {
    // disposition: "inline" or "attachment"
    String fallback = asciiFallback(fileName);
    String encoded = rfc5987Encode(fileName);
    // Use both filename (fallback) and filename* (RFC5987 UTF-8)
    return String.format(
        "%s; filename=\"%s\"; filename*=UTF-8''%s", disposition, fallback, encoded);
  }

  private static String ensureExtension(String fileName, String mimeType) {
    if (fileName == null) return "file";

    String lower = fileName.toLowerCase();

    if (mimeType == null) return fileName;

    if (mimeType.equals("application/pdf") && !lower.endsWith(".pdf")) {
      return fileName + ".pdf";
    } else if (mimeType.contains("word") && !lower.endsWith(".docx")) {
      return fileName + ".docx";
    } else if (mimeType.contains("excel") && !lower.endsWith(".xlsx")) {
      return fileName + ".xlsx";
    } else if (mimeType.contains("presentation") && !lower.endsWith(".pptx")) {
      return fileName + ".pptx";
    } else if (mimeType.startsWith("image/")) {
      String ext = mimeType.substring("image/".length());
      if (!lower.endsWith("." + ext)) {
        return fileName + "." + ext;
      }
    }

    return fileName;
  }
}
