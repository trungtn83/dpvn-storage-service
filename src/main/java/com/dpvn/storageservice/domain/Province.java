package com.dpvn.storageservice.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "province_master")
public class Province {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long idf;
  private String code; // mã hành chính
  private String name; // tỉnh Lào Cai

  @Column(columnDefinition = "TEXT")
  private String detailDescription; // 99 ĐVHC (10 phường, 89 xã)

  @Column(columnDefinition = "TEXT")
  private String oldDescription; // (trước đây là) tỉnh Yên Bái và tỉnh Lào Cai

  @Column(columnDefinition = "TEXT")
  private String administrativeCenter; // (trung tâm hành chính ở) Yên Bái (cũ)

  private long totalCitizen;
  private double totalAreaKm2;
  private String longitude; // kinh do
  private String latitude; // vi do
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private int version;

  @Column(columnDefinition = "TEXT")
  private String note;

  @OneToMany(
      mappedBy = "province",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<Ward> wards = new HashSet<>();

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

  public Set<Ward> getWards() {
    return wards;
  }

  public void setWards(Set<Ward> wards) {
    this.wards = wards;
  }

  public Long getIdf() {
    return idf;
  }

  public void setIdf(Long idf) {
    this.idf = idf;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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
