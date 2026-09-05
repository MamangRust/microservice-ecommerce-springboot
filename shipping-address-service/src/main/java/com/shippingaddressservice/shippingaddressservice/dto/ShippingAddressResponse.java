package com.shippingaddressservice.shippingaddressservice.dto;

import java.time.LocalDateTime;

public record ShippingAddressResponse(
    Long id, Long orderId, String alamat, String provinsi, String negara,
    String kota, String courier, String shippingMethod, Integer shippingCost,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}