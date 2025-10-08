package com.dpvn.storageservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDto {
  @JsonIgnore private Long id;
  private String slug; // uuid + ext
  @JsonIgnore private String filePath; // 20250915/abc.png
  private String fileName; // filename gốc upload lên
  @JsonIgnore private String fileMimeType; // image/png, video/mp4...
  @JsonIgnore private Long fileSize;
  @JsonIgnore private String source; // gốc từ cdn thuốc sỉ, hay upload lên, hay lấy từ đâu?
  @JsonIgnore private Instant createdAt;
  @JsonIgnore private Instant updateAt;
  @JsonIgnore private Long accessCount = 0L;
  @JsonIgnore private Instant lastAccessAt;
  @JsonIgnore private List<FileMetadataDto> metadata = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileMimeType() {
    return fileMimeType;
  }

  public void setFileMimeType(String fileType) {
    this.fileMimeType = fileType;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdateAt() {
    return updateAt;
  }

  public void setUpdateAt(Instant updateAt) {
    this.updateAt = updateAt;
  }

  public Long getAccessCount() {
    return accessCount;
  }

  public void setAccessCount(Long accessCount) {
    this.accessCount = accessCount;
  }

  public Instant getLastAccessAt() {
    return lastAccessAt;
  }

  public void setLastAccessAt(Instant lastAccessAt) {
    this.lastAccessAt = lastAccessAt;
  }

  public List<FileMetadataDto> getMetadata() {
    return metadata;
  }

  public void setMetadata(List<FileMetadataDto> metadata) {
    this.metadata = metadata;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}
