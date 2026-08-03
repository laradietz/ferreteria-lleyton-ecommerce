package com.ecommerce.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    String type,
    String subject,
    String body,
    Boolean read,
    LocalDateTime createdAt
) {}
