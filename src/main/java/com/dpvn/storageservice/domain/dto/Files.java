package com.dpvn.storageservice.domain.dto;

public class Files {
  private Files() {}

  public static class Source {
    private Source() {}

    public static final int THUOCSI = 1;
    public static final int KIOTVIET = 2;
    public static final int WEBSITE = 3;
    public static final int CRM = 4;

    public static final int OTHER = 0;
  }
}
