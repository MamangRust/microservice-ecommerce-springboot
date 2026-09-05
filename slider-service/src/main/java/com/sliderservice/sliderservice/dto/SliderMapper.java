package com.sliderservice.sliderservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.sliderservice.sliderservice.entity.Slider;

@Mapper(componentModel = ComponentModel.SPRING)
public interface SliderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Slider toEntity(SliderRequest request);
    SliderResponse toResponse(Slider entity);
}