package com.dpvn.storageservice.service;

import com.dpvn.storageservice.domain.HttpDownloadResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.tika.Tika;

public class FileUtil {

  private static final Tika tika = new Tika();

  /**
   * Xác định mime type một cách an toàn, có fallback nếu stream không reset được
   */
  public static String getMimeType(String responseContentType, InputStream input) {
    if (responseContentType != null && !responseContentType.isBlank()) {
      return responseContentType;
    }

    try {
      InputStream stream = input.markSupported() ? input : new BufferedInputStream(input);
      stream.mark(8192);
      String detected = tika.detect(stream);
      try {
        stream.reset();
      } catch (IOException resetErr) {
        // fallback: đọc lại từ buffer nếu không reset được
        detected = detectFromBuffer(input);
      }
      return (detected != null && !detected.isBlank()) ? detected : "application/octet-stream";
    } catch (Exception e) {
      return "application/octet-stream";
    }
  }

  /**
   * Fallback khi stream không reset được: đọc tối đa 8KB vào buffer rồi detect
   */
  private static String detectFromBuffer(InputStream input) {
    try {
      byte[] buf = input.readNBytes(8192);
      return tika.detect(buf);
    } catch (IOException e) {
      return "application/octet-stream";
    }
  }

  /**
   * Sinh slug ngẫu nhiên cho file
   */
  public static String getSlug() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * Lấy extension từ MIME hoặc tên file
   */
  public static String getExtensionFromMimeType(String mimeType, String fileName) {
    if (fileName != null && fileName.contains(".")) {
      return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
    if (mimeType == null) return "bin";
    if (mimeType.equals("image/jpeg")) return "jpg";
    if (mimeType.equals("image/png")) return "png";
    if (mimeType.equals("application/pdf")) return "pdf";
    if (mimeType.startsWith("text/")) return "txt";
    return "bin";
  }

  /**
   * Tạo đường dẫn tương đối cho file
   */
  public static String getRelative(String fileName) {
    String prefix = fileName.substring(0, 2);
    return prefix + "/" + fileName;
  }

  /**
   * Download file HTTP (đơn giản)
   */
  public static HttpDownloadResult download(String urlStr) throws IOException {
    // Nếu URL có ký tự Unicode thì encode phần path
    int lastSlash = urlStr.lastIndexOf('/');
    String base = urlStr.substring(0, lastSlash + 1);
    String file = urlStr.substring(lastSlash + 1);
    String encodedFile = URLEncoder.encode(file, StandardCharsets.UTF_8).replace("+", "%20");
    URL url = new URL(base + encodedFile);

    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setInstanceFollowRedirects(true);
    conn.setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36");
    conn.setRequestProperty("Accept", "application/pdf,image/jpeg,image/png,*/*;q=0.8");
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(10000);

    String contentType = conn.getContentType();
    String disposition = conn.getHeaderField("Content-Disposition");
    String fileName = null;
    if (disposition != null && disposition.contains("filename=")) {
      fileName = disposition.split("filename=")[1].replace("\"", "").trim();
    } else {
      fileName = urlStr.substring(urlStr.lastIndexOf('/') + 1);
    }

    long size = conn.getContentLengthLong();
    InputStream in = conn.getInputStream();
    return new HttpDownloadResult(in, fileName, contentType, size);
  }
}
