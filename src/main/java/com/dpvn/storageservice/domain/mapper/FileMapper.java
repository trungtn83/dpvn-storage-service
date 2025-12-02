package com.dpvn.storageservice.domain.mapper;

import com.dpvn.sharedcore.domain.mapper.BaseMapper;
import com.dpvn.storageservice.domain.dto.FileDto;
import com.dpvn.storageservice.domain.entity.File;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper extends BaseMapper<File, FileDto> {}
