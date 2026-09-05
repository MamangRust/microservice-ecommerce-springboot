package com.reviewdetailservice.reviewdetailservice.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.reviewdetailservice.reviewdetailservice.entity.ReviewDetail;

@Mapper(componentModel = ComponentModel.SPRING)
public interface ReviewDetailMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ReviewDetail toEntity(ReviewDetailRequest request);
    ReviewDetailResponse toResponse(ReviewDetail entity);
}