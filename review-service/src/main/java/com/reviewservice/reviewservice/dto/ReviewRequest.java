package com.reviewservice.reviewservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
    @NotNull Long userId,
    @NotNull Long productId,
    String name,
    String comment,
    @NotNull @Min(1) @Max(5) Integer rating
) {}