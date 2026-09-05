package com.merchantdetailservice.merchantdetailservice.dto;

import java.time.LocalDateTime;

public record MerchantDetailResponse(
    Long id, Long merchantId, String displayName, String coverImageUrl,
    String logoUrl, String shortDescription, String websiteUrl,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}