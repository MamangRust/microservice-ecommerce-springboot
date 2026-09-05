package com.merchantpolicyservice.merchantpolicyservice.dto;

import jakarta.validation.constraints.NotNull;

public record MerchantPolicyRequest(
    @NotNull Long merchantId,
    String policyType,
    String title,
    String description
) {}