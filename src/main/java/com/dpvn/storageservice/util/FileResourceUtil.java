package com.dpvn.storageservice.util;

import com.dpvn.storageservice.domain.dto.NamedByteArrayResource;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;

public class FileResourceUtil {

  /**
   * Tạo Resource từ nội dung String, dùng để upload qua Feign multipart.
   * @param content nội dung file (text hoặc HTML)
   * @param filename tên file (tự động thêm đuôi nếu chưa có)
   * @return Resource sẵn sàng dùng cho Multipart upload
   */
  public static Resource toMultipartResource(String content, String filename) {
    if (content == null || content.isEmpty()) {
      throw new IllegalArgumentException("content must not be null or empty");
    }
    if (filename == null || filename.isEmpty()) {
      throw new IllegalArgumentException("filename must not be null or empty");
    }

    final String finalFilename = filename.endsWith(".html") ? filename : filename + ".html";
    final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

    return new NamedByteArrayResource(bytes, finalFilename);
  }
}
