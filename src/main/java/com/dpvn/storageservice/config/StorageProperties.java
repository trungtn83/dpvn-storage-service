package com.dpvn.storageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
  private String basePath;
  private String strategy; // flat | date
  private String urlPrefix;

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  public String getStrategy() {
    return strategy;
  }

  public void setStrategy(String strategy) {
    this.strategy = strategy;
  }

  public String getUrlPrefix() {
    return urlPrefix;
  }

  public void setUrlPrefix(String urlPrefix) {
    this.urlPrefix = urlPrefix;
  }
}
