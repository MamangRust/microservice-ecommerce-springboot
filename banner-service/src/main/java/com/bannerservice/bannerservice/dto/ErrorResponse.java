package com.bannerservice.bannerservice.dto;

public record ErrorResponse(int status, String error, String message, String path) {}