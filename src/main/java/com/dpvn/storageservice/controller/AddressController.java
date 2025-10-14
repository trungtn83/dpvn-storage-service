package com.dpvn.storageservice.controller;

import com.dpvn.shared.domain.BeanMapper;
import com.dpvn.storageservice.domain.Province;
import com.dpvn.storageservice.domain.ProvinceDto;
import com.dpvn.storageservice.service.AddressService;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address")
public class AddressController {
  private final Logger LOGGER = LoggerFactory.getLogger(AddressController.class);

  private final AddressService addressService;

  public AddressController(AddressService addressService) {
    this.addressService = addressService;
  }

  @PostMapping("/sync-all")
  public void syncAll() {}

  @GetMapping("/province")
  public List<ProvinceDto> getAllProvinces() {
    return addressService.findAll().stream()
        .map(
            address -> {
              ProvinceDto dto = BeanMapper.instance().map(address, ProvinceDto.class);
              dto.setWards(new HashSet<>());
              return dto;
            })
        .toList();
  }

  @GetMapping("/province/{id}")
  public ProvinceDto getProvinceById(@PathVariable Long id) {
    Province province = addressService.findById(id);
    if (province == null) {
      return null;
    }
    return BeanMapper.instance().map(province, ProvinceDto.class);
  }
}
