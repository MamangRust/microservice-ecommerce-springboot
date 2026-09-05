package com.sliderservice.sliderservice.dto;

public record ErrorResponse(int status, String error, String message, String path) {}