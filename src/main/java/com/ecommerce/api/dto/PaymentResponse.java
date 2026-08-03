package com.ecommerce.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
    UUID id,
    UUID orderId,
    BigDecimal amount,
    String method,
    String status,
    String transactionReference,
    LocalDateTime processedAt
) {}
