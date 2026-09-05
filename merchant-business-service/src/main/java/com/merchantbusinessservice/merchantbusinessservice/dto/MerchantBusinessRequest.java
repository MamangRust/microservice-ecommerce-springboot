package com.merchantbusinessservice.merchantbusinessservice.dto;

import jakarta.validation.constraints.NotNull;

public record MerchantBusinessRequest(
    @NotNull Long merchantId,
    String businessType,
    String taxId,
    Integer establishedYear,
    Integer numberOfEmployees,
    String websiteUrl
) {}