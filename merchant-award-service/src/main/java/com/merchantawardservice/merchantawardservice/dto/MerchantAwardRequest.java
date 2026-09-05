package com.merchantawardservice.merchantawardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MerchantAwardRequest(
    @NotNull Long merchantId,
    @NotBlank String title,
    String description,
    String issuedBy,
    LocalDate issueDate,
    LocalDate expiryDate,
    String certificateUrl
) {}