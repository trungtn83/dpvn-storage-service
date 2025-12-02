package com.dpvn.storageservice.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "file")
public class File {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String slug; // uuid + ext

  @Column(columnDefinition = "TEXT")
  private String filePath; // 20250915/abc.png

  private String fileName; // filename gốc upload lên

  private String fileMimeType; // image/png, video/mp4...

  private Long fileSize;

  private String source; // gốc từ cdn thuốc sỉ, hay upload lên, hay lấy từ đâu?

  @Column(columnDefinition = "TEXT")
  private String hash; // hash md5 check file upload nhiều lần

  private Instant createdAt;

  private Instant updateAt;

  @Column private Long accessCount = 0L;

  private Instant lastAccessAt;

  @OneToMany(
      mappedBy = "file",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<FileMetadata> metadata = new ArrayList<>();

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

  public List<FileMetadata> getMetadata() {
    return metadata;
  }

  public void setMetadata(List<FileMetadata> metadata) {
    this.metadata = metadata;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }
}
