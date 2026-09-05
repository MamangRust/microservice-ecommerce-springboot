package com.shippingaddressservice.shippingaddressservice.dto;

public record ErrorResponse(int status, String error, String message, String path) {}