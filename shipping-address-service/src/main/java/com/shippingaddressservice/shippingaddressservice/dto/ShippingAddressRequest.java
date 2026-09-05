package com.shippingaddressservice.shippingaddressservice.dto;

import jakarta.validation.constraints.NotNull;

public record ShippingAddressRequest(
    @NotNull Long orderId,
    String alamat,
    String provinsi,
    String negara,
    String kota,
    String courier,
    String shippingMethod,
    Integer shippingCost
) {}