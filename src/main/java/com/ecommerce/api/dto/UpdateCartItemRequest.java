package com.ecommerce.api.dto;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(
    @Min(1) int quantity
) {}
