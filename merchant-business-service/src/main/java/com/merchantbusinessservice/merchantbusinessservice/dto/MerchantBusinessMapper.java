package com.merchantbusinessservice.merchantbusinessservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.merchantbusinessservice.merchantbusinessservice.entity.MerchantBusinessInformation;

@Mapper(componentModel = ComponentModel.SPRING)
public interface MerchantBusinessMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    MerchantBusinessInformation toEntity(MerchantBusinessRequest request);
    MerchantBusinessResponse toResponse(MerchantBusinessInformation entity);
}