package com.shippingaddressservice.shippingaddressservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.shippingaddressservice.shippingaddressservice.entity.ShippingAddress;

@Mapper(componentModel = ComponentModel.SPRING)
public interface ShippingAddressMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ShippingAddress toEntity(ShippingAddressRequest request);
    ShippingAddressResponse toResponse(ShippingAddress entity);
}