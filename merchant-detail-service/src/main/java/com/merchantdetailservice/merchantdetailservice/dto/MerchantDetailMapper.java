package com.merchantdetailservice.merchantdetailservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.merchantdetailservice.merchantdetailservice.entity.MerchantDetail;

@Mapper(componentModel = ComponentModel.SPRING)
public interface MerchantDetailMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    MerchantDetail toEntity(MerchantDetailRequest request);
    MerchantDetailResponse toResponse(MerchantDetail entity);
}