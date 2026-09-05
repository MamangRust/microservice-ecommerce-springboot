package com.merchantawardservice.merchantawardservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.merchantawardservice.merchantawardservice.entity.MerchantCertificationAndAward;

@Mapper(componentModel = ComponentModel.SPRING)
public interface MerchantAwardMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    MerchantCertificationAndAward toEntity(MerchantAwardRequest request);

    MerchantAwardResponse toResponse(MerchantCertificationAndAward entity);
}