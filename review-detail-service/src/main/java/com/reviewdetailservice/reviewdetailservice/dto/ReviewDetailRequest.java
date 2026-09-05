package com.reviewdetailservice.reviewdetailservice.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewDetailRequest(
    @NotNull Long reviewId,
    String type,
    String url,
    String caption
) {}