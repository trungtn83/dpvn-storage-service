package com.dpvn.storageservice.domain.mapper;

import com.dpvn.sharedcore.domain.mapper.BaseMapper;
import com.dpvn.storageservice.domain.dto.ProvinceDto;
import com.dpvn.storageservice.domain.entity.Province;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProvinceMapper extends BaseMapper<Province, ProvinceDto> {}
