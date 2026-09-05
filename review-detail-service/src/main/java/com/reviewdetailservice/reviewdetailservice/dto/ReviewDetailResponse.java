package com.reviewdetailservice.reviewdetailservice.dto;

import java.time.LocalDateTime;

public record ReviewDetailResponse(
    Long id, Long reviewId, String type, String url, String caption,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}