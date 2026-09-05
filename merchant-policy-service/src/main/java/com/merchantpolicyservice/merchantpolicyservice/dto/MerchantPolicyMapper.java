package com.merchantpolicyservice.merchantpolicyservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.merchantpolicyservice.merchantpolicyservice.entity.MerchantPolicy;

@Mapper(componentModel = ComponentModel.SPRING)
public interface MerchantPolicyMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    MerchantPolicy toEntity(MerchantPolicyRequest request);
    MerchantPolicyResponse toResponse(MerchantPolicy entity);
}