package com.merchantbusinessservice.merchantbusinessservice.dto;

import java.time.LocalDateTime;

public record MerchantBusinessResponse(
    Long id, Long merchantId, String businessType, String taxId,
    Integer establishedYear, Integer numberOfEmployees, String websiteUrl,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}