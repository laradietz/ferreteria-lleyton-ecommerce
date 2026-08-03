package com.ecommerce.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
    UUID id,
    UUID productId,
    String productName,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal subtotal
) {}
