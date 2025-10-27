package com.dpvn.storageservice.repository;

import com.dpvn.storageservice.domain.entity.Province;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvinceRepository extends JpaRepository<Province, Long> {
  List<Province> findByIdfIn(List<Long> idf);
}
