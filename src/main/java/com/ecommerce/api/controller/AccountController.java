package com.ecommerce.api.controller;

import com.ecommerce.api.dto.UpdateMyAccountRequest;
import com.ecommerce.api.dto.UserResponse;
import com.ecommerce.api.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Mi cuenta")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyAccount(Authentication authentication) {
        return ResponseEntity.ok(accountService.getMyAccount(authentication.getName()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyAccount(
            Authentication authentication,
            @Valid @RequestBody UpdateMyAccountRequest request) {
        return ResponseEntity.ok(accountService.updateMyAccount(authentication.getName(), request));
    }
}
