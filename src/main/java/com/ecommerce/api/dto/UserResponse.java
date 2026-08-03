package com.ecommerce.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    String phone,
    String address,
    Boolean active,
    String role,
    LocalDateTime createdAt
) {}
