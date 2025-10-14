package com.dpvn.storageservice.domain;

import java.time.LocalDateTime;

public class WardDto {
  private Long id;
  private Long idf;
  private String code; // mã hành chính
  private String type; // phường hay xã
  private String name; // Ba Đinh
  private String detailDescription;
  private String
      oldDescription; // (trước đây là) "Xã Tam Hiệp (huyện Thanh Trì) (phần còn lại sau khi sáp
  // nhập vào phường Hoàng Liệt), Xã Hữu Hòa (phần còn lại sau khi sáp nhập vào
  // phường Phú Lương), Phường Kiến Hưng (phần còn lại sau khi sáp nhập vào
  // phường Phú Lương, phường Kiến Hưng), Thị trấn Văn Điển (phần còn lại sau
  // khi sáp nhập vào phường Hoàng Liệt, xã Thanh Trì), Xã Tả Thanh Oai (phần
  // còn lại sau khi sáp nhập vào phường Thanh Liệt), Xã Vĩnh Quỳnh (phần còn
  // lại sau khi sáp nhập vào xã Thanh Trì)"
  private String administrativeCenter; // (trung tâm hành chính ở) "Thôn Quỳnh Đô, xã Đại Thanh"
  private long totalCitizen;
  private double totalAreaKm2;
  private String longitude; // kinh do
  private String latitude; // vi do
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private int version;
  private String note;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdf() {
    return idf;
  }

  public void setIdf(Long idf) {
    this.idf = idf;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDetailDescription() {
    return detailDescription;
  }

  public void setDetailDescription(String detailDescription) {
    this.detailDescription = detailDescription;
  }

  public String getOldDescription() {
    return oldDescription;
  }

  public void setOldDescription(String oldDescription) {
    this.oldDescription = oldDescription;
  }

  public String getAdministrativeCenter() {
    return administrativeCenter;
  }

  public void setAdministrativeCenter(String administrativeCenter) {
    this.administrativeCenter = administrativeCenter;
  }

  public long getTotalCitizen() {
    return totalCitizen;
  }

  public void setTotalCitizen(long totalCitizen) {
    this.totalCitizen = totalCitizen;
  }

  public double getTotalAreaKm2() {
    return totalAreaKm2;
  }

  public void setTotalAreaKm2(double totalAreaKm2) {
    this.totalAreaKm2 = totalAreaKm2;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
