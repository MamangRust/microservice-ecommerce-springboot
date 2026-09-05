package com.cartservice.cartservice.dto;

import java.time.LocalDateTime;

public record CartResponse(
    Long id, Long userId, Long productId, String name, Integer price,
    String image, Integer quantity, Integer weight,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}