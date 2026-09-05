package com.bannerservice.bannerservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BannerResponse(
    Long id, String name, LocalDate startDate, LocalDate endDate,
    LocalTime startTime, LocalTime endTime, Boolean isActive,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}