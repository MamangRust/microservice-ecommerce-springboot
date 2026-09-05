package com.cartservice.cartservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.cartservice.cartservice.entity.Cart;

@Mapper(componentModel = ComponentModel.SPRING)
public interface CartMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Cart toEntity(CartRequest request);
    CartResponse toResponse(Cart entity);
}