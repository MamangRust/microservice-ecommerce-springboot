package com.merchantbusinessservice.merchantbusinessservice.dto;

public record ErrorResponse(int status, String error, String message, String path) {}