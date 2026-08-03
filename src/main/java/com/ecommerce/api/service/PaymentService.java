package com.ecommerce.api.service;

import com.ecommerce.api.dto.CreatePaymentRequest;
import com.ecommerce.api.dto.PaymentResponse;
import com.ecommerce.api.entity.*;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;

    public PaymentResponse findByOrder(String username, boolean isAdmin, UUID orderId) {
        Order order = orderService.findEntity(orderId);
        if (!isAdmin && !order.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Pago para el pedido", orderId);
        }
        return toResponse(paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago para el pedido", orderId)));
    }

    /**
     * Simula el procesamiento de un pago (no hay integración real con una
     * pasarela). Queda aprobado salvo que el monto sea cero o negativo.
     */
    @Transactional
    public PaymentResponse process(String username, CreatePaymentRequest req) {
        Order order = orderService.findEntity(req.orderId());

        if (!order.getUser().getUsername().equals(username)) {
            throw new BusinessException("Este pedido no te pertenece");
        }
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BusinessException("Este pedido ya tiene un pago registrado");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Solo se pueden pagar pedidos en estado PENDING");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(req.method());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Método de pago inválido: " + req.method());
        }

        boolean approved = order.getTotalAmount().signum() > 0;

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(method)
                .status(approved ? PaymentStatus.APPROVED : PaymentStatus.REJECTED)
                .transactionReference(UUID.randomUUID().toString())
                .processedAt(LocalDateTime.now())
                .build();
        payment = paymentRepository.save(payment);

        if (approved) {
            orderService.updateStatus(order.getId(), OrderStatus.CONFIRMED);
            notificationService.send(order.getUser(), NotificationType.PAYMENT_APPROVED,
                    "Pago aprobado", "El pago de tu pedido #" + order.getId() + " fue aprobado.");
        } else {
            notificationService.send(order.getUser(), NotificationType.PAYMENT_REJECTED,
                    "Pago rechazado", "El pago de tu pedido #" + order.getId() + " fue rechazado.");
        }

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrder().getId(), p.getAmount(),
                p.getMethod().name(), p.getStatus().name(), p.getTransactionReference(),
                p.getProcessedAt());
    }
}
