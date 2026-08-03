package com.ecommerce.api.controller;

import com.ecommerce.api.dto.ShipmentResponse;
import com.ecommerce.api.dto.UpdateShipmentRequest;
import com.ecommerce.api.service.ShipmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Tag(name = "Envíos")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/pedido/{orderId}")
    public ResponseEntity<ShipmentResponse> findByOrder(Authentication authentication,
                                                          @PathVariable UUID orderId) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(shipmentService.findByOrder(authentication.getName(), isAdmin, orderId));
    }

    @PutMapping("/pedido/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipmentResponse> createOrUpdate(@PathVariable UUID orderId,
                                                             @Valid @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.createOrUpdate(orderId, request));
    }
}
