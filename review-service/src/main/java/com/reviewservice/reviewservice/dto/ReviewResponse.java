package com.reviewservice.reviewservice.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
    Long id, Long userId, Long productId, String name, String comment,
    Integer rating, LocalDateTime createdAt, LocalDateTime updatedAt
) {}