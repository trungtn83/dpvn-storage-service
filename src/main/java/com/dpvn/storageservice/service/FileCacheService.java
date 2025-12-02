package com.dpvn.storageservice.service;

import com.dpvn.sharedcore.config.CacheService;
import com.dpvn.sharedcore.util.FastMap;
import com.dpvn.storageservice.domain.entity.File;
import org.springframework.stereotype.Service;

@Service
public class FileCacheService {
  public static final String KEY_FILE_METADATA_FORMAT = "dpvn-storage:file:meta:%s";
  private final CacheService cacheService;

  public FileCacheService(CacheService cacheService) {
    this.cacheService = cacheService;
  }

  public void putMeta(File file) {
    String key = String.format(KEY_FILE_METADATA_FORMAT, file.getSlug());
    FastMap value =
        FastMap.create()
            .add("slug", file.getFilePath())
            .add("filePath", file.getFilePath())
            .add("fileName", file.getFileName())
            .add("fileSize", file.getFileSize())
            .add("fileMimeType", file.getFileMimeType());
    cacheService.setValue(key, value, 4 * 60);
  }

  public File getMeta(String slug) {
    String key = String.format(KEY_FILE_METADATA_FORMAT, slug);
    if (cacheService.hasKey(key)) {
      return cacheService.getValue(key, File.class);
    }
    return null;
  }

  public void evictMeta(String slug) {
    String key = String.format(KEY_FILE_METADATA_FORMAT, slug);
    cacheService.removeKey(key);
  }
}
