package com.ecommerce.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentRequest(
    @NotNull UUID orderId,
    @NotNull String method
) {}
