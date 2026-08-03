package com.ecommerce.api.controller;

import com.ecommerce.api.dto.CreateOrderRequest;
import com.ecommerce.api.dto.OrderResponse;
import com.ecommerce.api.entity.OrderStatus;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findMine(Authentication authentication) {
        return ResponseEntity.ok(orderService.findMine(authentication.getName()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(Authentication authentication, @PathVariable UUID id) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(orderService.findById(id, authentication.getName(), isAdmin));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(Authentication authentication,
                                                  @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createFromCart(authentication.getName(), request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id,
                                                        @RequestParam String status) {
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado de pedido inválido: " + status);
        }
        return ResponseEntity.ok(orderService.updateStatus(id, newStatus));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<OrderResponse> cancel(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelMine(id, authentication.getName()));
    }
}
