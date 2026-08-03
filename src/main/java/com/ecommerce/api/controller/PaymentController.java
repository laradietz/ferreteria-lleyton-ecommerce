package com.ecommerce.api.controller;

import com.ecommerce.api.dto.CreatePaymentRequest;
import com.ecommerce.api.dto.PaymentResponse;
import com.ecommerce.api.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Pagos")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/pedido/{orderId}")
    public ResponseEntity<PaymentResponse> findByOrder(Authentication authentication,
                                                         @PathVariable UUID orderId) {
        boolean isAdmin = isAdmin(authentication);
        return ResponseEntity.ok(paymentService.findByOrder(authentication.getName(), isAdmin, orderId));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> process(Authentication authentication,
                                                     @Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.process(authentication.getName(), request));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
