package com.sliderservice.sliderservice.dto;

import java.time.LocalDateTime;
public record SliderResponse(Long id, String name, String image, LocalDateTime createdAt, LocalDateTime updatedAt) {}