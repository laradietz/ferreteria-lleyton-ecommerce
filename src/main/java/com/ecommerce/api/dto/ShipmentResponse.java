package com.ecommerce.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponse(
    UUID id,
    UUID orderId,
    String carrier,
    String trackingNumber,
    String status,
    LocalDateTime shippedAt,
    LocalDateTime deliveredAt
) {}
