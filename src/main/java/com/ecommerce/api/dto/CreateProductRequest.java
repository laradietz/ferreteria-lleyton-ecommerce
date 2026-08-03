package com.ecommerce.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
    @NotBlank String sku,
    @NotBlank String name,
    String description,
    @NotNull @DecimalMin("0.0") BigDecimal price,
    @NotNull @Min(0) Integer stock,
    String imageUrl,
    @NotNull UUID categoryId
) {}
