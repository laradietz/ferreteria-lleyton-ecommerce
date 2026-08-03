package com.ecommerce.api.service;

import com.ecommerce.api.dto.*;
import com.ecommerce.api.entity.*;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final NotificationService notificationService;

    public List<OrderResponse> findMine(String username) {
        User user = findUser(username);
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    /** El propio cliente ve el detalle solo de sus pedidos; un ADMIN puede ver cualquiera. */
    public OrderResponse findById(UUID id, String username, boolean isAdmin) {
        Order order = findEntity(id);
        verifyOwnership(order, username, isAdmin);
        return toResponse(order);
    }

    /**
     * Crea un pedido a partir del carrito actual del usuario: congela precios,
     * descuenta stock, vacía el carrito y dispara una notificación.
     */
    @Transactional
    public OrderResponse createFromCart(String username, CreateOrderRequest req) {
        User user = findUser(username);
        Cart cart = cartService.findOrCreateCart(username);

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("El carrito está vacío");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress(req.shippingAddress())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException("No hay suficiente stock de " + product.getName());
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            order.getItems().add(orderItem);
            total = total.add(subtotal);

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }
        order.setTotalAmount(total);
        order = orderRepository.save(order);

        cartService.clear(cart);

        notificationService.send(user, NotificationType.ORDER_CREATED,
                "Pedido recibido",
                "Tu pedido #" + order.getId() + " fue creado por un total de $" + total + ".");

        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, OrderStatus newStatus) {
        Order order = findEntity(orderId);
        order.setStatus(newStatus);
        order = orderRepository.save(order);

        NotificationType type = switch (newStatus) {
            case SHIPPED -> NotificationType.ORDER_SHIPPED;
            case DELIVERED -> NotificationType.ORDER_DELIVERED;
            case CANCELLED -> NotificationType.ORDER_CANCELLED;
            default -> null;
        };
        if (type != null) {
            notificationService.send(order.getUser(), type,
                    "Actualización de tu pedido",
                    "Tu pedido #" + order.getId() + " ahora está: " + newStatus + ".");
        }
        return toResponse(order);
    }

    /** El propio cliente cancela su pedido pendiente (nunca el de otro usuario). */
    @Transactional
    public OrderResponse cancelMine(UUID id, String username) {
        Order order = findEntity(id);
        verifyOwnership(order, username, false);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Solo se pueden cancelar pedidos en estado PENDING");
        }
        return updateStatus(id, OrderStatus.CANCELLED);
    }

    private void verifyOwnership(Order order, String username, boolean isAdmin) {
        if (isAdmin) return;
        if (!order.getUser().getUsername().equals(username)) {
            // 404 en vez de 403: no revelamos que el pedido existe si no es tuyo.
            throw new ResourceNotFoundException("Pedido", order.getId());
        }
    }

    Order findEntity(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));
    }

    private OrderResponse toResponse(Order o) {
        var items = o.getItems().stream().map(i -> new OrderItemResponse(
                i.getProduct().getId(), i.getProduct().getName(),
                i.getQuantity(), i.getUnitPrice(), i.getSubtotal())).toList();
        return new OrderResponse(o.getId(), o.getStatus().name(), o.getTotalAmount(),
                o.getShippingAddress(), items, o.getCreatedAt());
    }
}
