package com.dpvn.storageservice.service;

import com.dpvn.shared.service.AbstractService;
import com.dpvn.shared.util.ObjectUtil;
import com.dpvn.storageservice.domain.entity.Province;
import com.dpvn.storageservice.domain.entity.Ward;
import com.dpvn.storageservice.repository.ProvinceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AddressService extends AbstractService {
  private final ProvinceRepository provinceRepository;

  public AddressService(ProvinceRepository provinceRepository) {
    this.provinceRepository = provinceRepository;
  }

  public void syncAll(List<Province> entities) {
    List<Long> provinceIdfs = entities.stream().map(Province::getIdf).toList();
    Map<Long, Province> dbProvinces =
        provinceRepository.findByIdfIn(provinceIdfs).stream()
            .collect(Collectors.toMap(Province::getIdf, province -> province));

    List<Province> provinces = new ArrayList<>();
    for (Province entity : entities) {
      Province dbProvince = dbProvinces.get(entity.getIdf());
      if (dbProvince == null) {
        entity
            .getWards()
            .forEach(
                ward -> {
                  ward.setProvince(entity);
                });
        provinces.add(entity);
      } else {
        ObjectUtil.assign(entity, dbProvince, List.of("wards"));
        dbProvince.getWards().clear();
        Set<Ward> wards =
            entity.getWards().stream()
                .peek(w -> w.setProvince(dbProvince))
                .collect(Collectors.toSet());
        dbProvince.getWards().addAll(wards);
      }
    }
    provinceRepository.saveAll(provinces);
  }

  public List<Province> findAll() {
    return provinceRepository.findAll();
  }

  public Province findById(Long id) {
    return provinceRepository.findById(id).orElse(null);
  }
}
