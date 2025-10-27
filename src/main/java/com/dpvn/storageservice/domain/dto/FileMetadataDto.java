package com.dpvn.storageservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileMetadataDto {
  private String metaKey; // ví dụ: width, height, duration, dpi...
  private String metaValue; // lưu dưới dạng String cho linh hoạt

  public String getMetaKey() {
    return metaKey;
  }

  public void setMetaKey(String metaKey) {
    this.metaKey = metaKey;
  }

  public String getMetaValue() {
    return metaValue;
  }

  public void setMetaValue(String metaValue) {
    this.metaValue = metaValue;
  }
}
