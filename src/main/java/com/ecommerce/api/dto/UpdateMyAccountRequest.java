package com.ecommerce.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMyAccountRequest(
    @NotBlank String currentPassword,
    @Size(min = 3, max = 80) String newUsername,
    @Size(min = 8) String newPassword,
    String firstName,
    String lastName,
    String phone,
    String address
) {}
