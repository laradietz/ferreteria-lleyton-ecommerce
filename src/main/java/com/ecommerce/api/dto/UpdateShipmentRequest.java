package com.ecommerce.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateShipmentRequest(
    @NotBlank String carrier,
    @NotBlank String trackingNumber,
    @NotBlank String status
) {}
