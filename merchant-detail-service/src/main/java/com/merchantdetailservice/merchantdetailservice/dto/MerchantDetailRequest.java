package com.merchantdetailservice.merchantdetailservice.dto;

import jakarta.validation.constraints.NotNull;

public record MerchantDetailRequest(
    @NotNull Long merchantId,
    String displayName,
    String coverImageUrl,
    String logoUrl,
    String shortDescription,
    String websiteUrl
) {}