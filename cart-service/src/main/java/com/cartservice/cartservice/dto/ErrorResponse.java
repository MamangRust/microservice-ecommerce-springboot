package com.cartservice.cartservice.dto;

public record ErrorResponse(int status, String error, String message, String path) {}