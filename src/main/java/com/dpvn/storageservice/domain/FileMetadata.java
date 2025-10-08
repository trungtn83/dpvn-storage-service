package com.dpvn.storageservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id", nullable = false)
  private File file;

  @Column(nullable = false)
  private String metaKey; // ví dụ: width, height, duration, dpi...

  @Column(nullable = false)
  private String metaValue; // lưu dưới dạng String cho linh hoạt

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

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
