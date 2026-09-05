package com.bannerservice.bannerservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.bannerservice.bannerservice.entity.Banner;

@Mapper(componentModel = ComponentModel.SPRING)
public interface BannerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Banner toEntity(BannerRequest request);
    BannerResponse toResponse(Banner entity);
}