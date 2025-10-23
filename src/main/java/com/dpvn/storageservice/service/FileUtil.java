package com.dpvn.storageservice.service;

import com.dpvn.storageservice.domain.HttpDownloadResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
    try {
      URL url = new URL(percentEncodeNonAscii(urlStr));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setInstanceFollowRedirects(true); // Tự xử lý redirect
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(10000);

      conn.setRequestProperty(
          "User-Agent",
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36");
      conn.setRequestProperty(
          "Accept",
          "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
      conn.setRequestProperty("Accept-Language", "vi,en-US;q=0.9,en;q=0.8");

      int status = conn.getResponseCode();
      if (status != 200) {
        String err = conn.getErrorStream() != null
            ? new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8)
            : "";
        throw new IOException("Download failed: HTTP " + status + " - " + err);
      }

      String contentType = conn.getContentType();
      String disposition = conn.getHeaderField("Content-Disposition");
      String fileName;
      if (disposition != null && disposition.contains("filename=")) {
        fileName = disposition.split("filename=")[1].replace("\"", "").trim();
      } else {
        fileName = "downloaded_" + System.currentTimeMillis() + ".pdf";
      }

      long size = conn.getContentLengthLong();
      if (size <= 0) {
        // Không tin tưởng content-length = 0 => đọc hết vào buffer
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream in = conn.getInputStream()) {
          byte[] data = new byte[8192];
          int n;
          while ((n = in.read(data)) != -1) buffer.write(data, 0, n);
        }
        byte[] bytes = buffer.toByteArray();
        size = bytes.length;
        return new HttpDownloadResult(new ByteArrayInputStream(bytes), fileName, contentType, size);
      }

      InputStream in = conn.getInputStream();
      return new HttpDownloadResult(in, fileName, contentType, size);

    } catch (Exception e) {
      throw new IOException("Không tải được file từ URL: " + urlStr, e);
    }
  }

  /**
   * Chỉ percent-encode các ký tự non-ASCII và space.
   * - Giữ nguyên '%' nếu đã là percent-encoding (tránh double-encode)
   * - Không encode các kí tự ASCII hợp lệ như '?', '&', '=' vì chúng nằm trong query (không đến hàm này)
   */
  private static String percentEncodeNonAscii(String input) {
    if (input == null || input.isEmpty()) return input;

    StringBuilder sb = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); ) {
      int codePoint = input.codePointAt(i);
      char ch = input.charAt(i);

      // Nếu gặp '%' theo sau bởi 2 hex thì coi là đã encode trước -> copy nguyên
      if (ch == '%' && i + 2 < input.length()) {
        char c1 = input.charAt(i + 1);
        char c2 = input.charAt(i + 2);
        if (isHexChar(c1) && isHexChar(c2)) {
          sb.append('%').append(c1).append(c2);
          i += 3;
          continue;
        }
      }

      if (codePoint <= 127) {
        // ASCII: giữ nguyên, nhưng encode space thành %20
        if (codePoint == ' ') {
          sb.append("%20");
        } else {
          sb.append((char) codePoint);
        }
      } else {
        // Non-ASCII: percent-encode byte-by-byte trong UTF-8
        String toEncode = new String(Character.toChars(codePoint));
        byte[] bytes = toEncode.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
          sb.append('%');
          sb.append(String.format("%02X", b));
        }
      }
      i += Character.charCount(codePoint);
    }
    return sb.toString();
  }

  private static boolean isHexChar(char c) {
    return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
  }
}
