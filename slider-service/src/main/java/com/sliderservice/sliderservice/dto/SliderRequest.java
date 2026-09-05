package com.sliderservice.sliderservice.dto;

import jakarta.validation.constraints.NotBlank;
public record SliderRequest(@NotBlank String name, String image) {}