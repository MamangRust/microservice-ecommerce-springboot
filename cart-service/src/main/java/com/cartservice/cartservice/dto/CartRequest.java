package com.cartservice.cartservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartRequest(
    @NotNull Long userId,
    @NotNull Long productId,
    String name,
    Integer price,
    String image,
    @NotNull @Min(1) Integer quantity,
    Integer weight
) {}