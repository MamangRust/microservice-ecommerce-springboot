package com.merchantpolicyservice.merchantpolicyservice.dto;

import java.time.LocalDateTime;

public record MerchantPolicyResponse(
    Long id, Long merchantId, String policyType, String title, String description,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}