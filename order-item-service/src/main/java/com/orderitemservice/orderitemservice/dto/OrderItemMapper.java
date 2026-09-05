package com.orderitemservice.orderitemservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.orderitemservice.orderitemservice.entity.OrderItem;

@Mapper(componentModel = ComponentModel.SPRING)
public interface OrderItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    OrderItem toEntity(OrderItemRequest request);
    OrderItemResponse toResponse(OrderItem entity);
}