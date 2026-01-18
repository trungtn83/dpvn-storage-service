package com.dpvn.storageservice.domain.dto;

public class Folders {
  private Folders() {}

  private static final String ROOT_FOLDER = "dpvn";

  public static final String TEMP = ROOT_FOLDER + "/_temp";

  public static final String WEBSITE = ROOT_FOLDER + "/website";

  private static final String REPORT = ROOT_FOLDER + "/report";
  public static final String REPORT_REF = REPORT + "/references";
  public static final String REPORT_THUOCSI = REPORT + "/thuocsi";
  public static final String REPORT_KIOTVIET = REPORT + "/kiotviet";
  public static final String REPORT_MISA = REPORT + "/misa";
}
