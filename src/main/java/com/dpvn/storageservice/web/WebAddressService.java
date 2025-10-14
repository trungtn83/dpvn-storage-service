package com.dpvn.storageservice.web;

import com.dpvn.shared.domain.BeanMapper;
import com.dpvn.shared.util.FastMap;
import com.dpvn.shared.util.ObjectUtil;
import com.dpvn.shared.util.StringUtil;
import com.dpvn.storageservice.domain.Province;
import com.dpvn.storageservice.domain.ProvinceDto;
import com.dpvn.storageservice.domain.Ward;
import com.dpvn.storageservice.domain.WardDto;
import com.dpvn.storageservice.repository.ProvinceRepository;
import com.dpvn.storageservice.service.AddressService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class WebAddressService {
  private final BanDoClient banDoClient;
  private final ProvinceRepository provinceRepository;
  private final AddressService addressService;

  public WebAddressService(
      BanDoClient banDoClient,
      ProvinceRepository provinceRepository,
      AddressService addressService) {
    this.banDoClient = banDoClient;
    this.provinceRepository = provinceRepository;
    this.addressService = addressService;
  }

  private List<ProvinceDto> getAllProvinces() {
    String rawProvinces = banDoClient.getProvinces("id=0");
    List<FastMap> provinces = ObjectUtil.readValue(rawProvinces, new TypeReference<>() {});
    return provinces.stream()
        .map(
            p -> {
              ProvinceDto provinceDto = new ProvinceDto();
              provinceDto.setIdf(p.getLong("id"));
              provinceDto.setCode(p.getString("mahc"));
              provinceDto.setName(p.getString("tentinh"));
              provinceDto.setDetailDescription(p.getString("con"));
              provinceDto.setOldDescription(p.getString("truocsapnhap"));
              provinceDto.setAdministrativeCenter(p.getString("trungtamhc"));
              provinceDto.setTotalCitizen(toLong(p.getString("dansonguoi")));
              provinceDto.setTotalAreaKm2(toDouble(p.getString("dientichkm2")));
              provinceDto.setLongitude(p.getString("kinhdo"));
              provinceDto.setLatitude(p.getString("vido"));
              provinceDto.setCreatedAt(LocalDateTime.now());
              provinceDto.setUpdatedAt(LocalDateTime.now());
              return provinceDto;
            })
        .toList();
  }

  private List<WardDto> getAllWards(Long provinceId) {
    String rawWards = banDoClient.getWard("id=" + provinceId);
    List<FastMap> wards = ObjectUtil.readValue(rawWards, new TypeReference<>() {});
    return wards.stream()
        .map(
            w -> {
              WardDto wardDto = new WardDto();
              wardDto.setIdf(w.getLong("maxa"));
              wardDto.setCode(w.getString("ma"));
              wardDto.setType(w.getString("loai"));
              wardDto.setName(w.getString("tenhc"));
              wardDto.setDetailDescription("");
              wardDto.setOldDescription(w.getString("truocsapnhap"));
              wardDto.setAdministrativeCenter(w.getString("trungtamhc"));
              wardDto.setTotalCitizen(toLong(w.getString("dansonguoi")));
              wardDto.setTotalAreaKm2(toDouble(w.getString("dientichkm2")));
              wardDto.setLongitude(w.getString("kinhdo"));
              wardDto.setLatitude(w.getString("vido"));
              wardDto.setCreatedAt(LocalDateTime.now());
              wardDto.setUpdatedAt(LocalDateTime.now());

              return wardDto;
            })
        .toList();
  }

  private long toLong(String input) {
    if (StringUtil.isEmpty(input)) {
      return -1;
    }
    String standard = input.replaceAll("\\.", "").replaceAll(",", ".");
    try {
      return Long.parseLong(standard);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private double toDouble(String input) {
    if (StringUtil.isEmpty(input)) {
      return -1;
    }
    String standard = input.replaceAll("\\.", "").replaceAll(",", ".");
    try {
      return Double.parseDouble(standard);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public void syncAll() {
    List<ProvinceDto> allProvinces = getAllProvinces();

    List<Province> provinces = new ArrayList<>();
    for (ProvinceDto provinceDto : allProvinces) {
      Province province = BeanMapper.instance().map(provinceDto, Province.class);
      List<WardDto> wardDtos = getAllWards(province.getIdf());
      province.setWards(
          wardDtos.stream()
              .map(
                  dto -> {
                    return BeanMapper.instance().map(dto, Ward.class);
                  })
              .collect(Collectors.toSet()));
      provinces.add(province);
    }

    addressService.syncAll(provinces);
  }
}
