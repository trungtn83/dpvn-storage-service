package com.dpvn.storageservice.domain.mapper;

import com.dpvn.sharedcore.domain.mapper.BaseMapper;
import com.dpvn.storageservice.domain.dto.WardDto;
import com.dpvn.storageservice.domain.entity.Ward;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WardMapper extends BaseMapper<Ward, WardDto> {}
