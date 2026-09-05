package com.orderitemservice.orderitemservice.dto;

import java.time.LocalDateTime;

public record OrderItemResponse(
    Long id, Long orderId, Long productId, Integer quantity, Integer price,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}