package com.dpvn.storageservice.repository;

import com.dpvn.storageservice.domain.entity.File;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
  Optional<File> findBySlug(String slug);

  Optional<File> getFirstBySourceOrHash(String source, String hash);
}
