package com.ecommerce.api.dto;

public record LoginResponse(
    String token,
    String username,
    String role,
    long expiresInMs
) {}
