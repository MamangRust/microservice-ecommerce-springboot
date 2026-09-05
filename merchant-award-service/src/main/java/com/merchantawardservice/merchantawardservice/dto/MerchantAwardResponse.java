package com.merchantawardservice.merchantawardservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MerchantAwardResponse(
    Long id, Long merchantId, String title, String description,
    String issuedBy, LocalDate issueDate, LocalDate expiryDate,
    String certificateUrl, LocalDateTime createdAt, LocalDateTime updatedAt
) {}