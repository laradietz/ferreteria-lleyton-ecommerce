package com.ecommerce.api.service;

import com.ecommerce.api.dto.ShipmentResponse;
import com.ecommerce.api.dto.UpdateShipmentRequest;
import com.ecommerce.api.entity.*;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderService orderService;

    public ShipmentResponse findByOrder(String username, boolean isAdmin, UUID orderId) {
        Order order = orderService.findEntity(orderId);
        if (!isAdmin && !order.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Envío para el pedido", orderId);
        }
        return toResponse(shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Envío para el pedido", orderId)));
    }

    @Transactional
    public ShipmentResponse createOrUpdate(UUID orderId, UpdateShipmentRequest req) {
        Order order = orderService.findEntity(orderId);

        ShipmentStatus status;
        try {
            status = ShipmentStatus.valueOf(req.status());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado de envío inválido: " + req.status());
        }

        Shipment shipment = shipmentRepository.findByOrderId(orderId).orElse(null);
        if (shipment == null) {
            shipment = Shipment.builder().order(order).status(ShipmentStatus.PREPARING).build();
        }

        shipment.setCarrier(req.carrier());
        shipment.setTrackingNumber(req.trackingNumber());
        shipment.setStatus(status);
        if (status == ShipmentStatus.IN_TRANSIT && shipment.getShippedAt() == null) {
            shipment.setShippedAt(LocalDateTime.now());
        }
        if (status == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }
        shipment = shipmentRepository.save(shipment);

        if (status == ShipmentStatus.IN_TRANSIT) {
            orderService.updateStatus(orderId, OrderStatus.SHIPPED);
        } else if (status == ShipmentStatus.DELIVERED) {
            orderService.updateStatus(orderId, OrderStatus.DELIVERED);
        }

        return toResponse(shipment);
    }

    private ShipmentResponse toResponse(Shipment s) {
        return new ShipmentResponse(s.getId(), s.getOrder().getId(), s.getCarrier(),
                s.getTrackingNumber(), s.getStatus().name(), s.getShippedAt(), s.getDeliveredAt());
    }
}
