package com.dpvn.storageservice.domain;

import java.io.InputStream;

public class HttpDownloadResult {
  private InputStream inputStream;
  private String originalName;
  private String mimeType;
  private long size;

  public HttpDownloadResult(
      InputStream inputStream, String originalName, String mimeType, long size) {
    this.inputStream = inputStream;
    this.originalName = originalName;
    this.mimeType = mimeType;
    this.size = size;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }
}
