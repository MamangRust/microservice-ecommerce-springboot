package com.reviewservice.reviewservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.reviewservice.reviewservice.entity.Review;

@Mapper(componentModel = ComponentModel.SPRING)
public interface ReviewMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Review toEntity(ReviewRequest request);
    ReviewResponse toResponse(Review entity);
}