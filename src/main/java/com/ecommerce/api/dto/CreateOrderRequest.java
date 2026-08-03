package com.ecommerce.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
    @NotBlank String shippingAddress
) {}
