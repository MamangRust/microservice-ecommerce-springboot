package com.reviewdetailservice.reviewdetailservice.dto;

public record ErrorResponse(int status, String error, String message, String path) {}