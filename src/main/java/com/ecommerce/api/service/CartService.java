package com.ecommerce.api.service;

import com.ecommerce.api.dto.*;
import com.ecommerce.api.entity.*;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public CartResponse getMyCart(String username) {
        return toResponse(findOrCreateCart(username));
    }

    @Transactional
    public CartResponse addItem(String username, AddCartItemRequest req) {
        Cart cart = findOrCreateCart(username);
        Product product = productService.findEntity(req.productId());

        if (!product.getActive()) {
            throw new BusinessException("Ese producto ya no está disponible");
        }
        if (product.getStock() < req.quantity()) {
            throw new BusinessException("No hay suficiente stock de " + product.getName());
        }

        var existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + req.quantity());
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart).product(product).quantity(req.quantity()).build();
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }
        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponse updateItem(String username, UUID itemId, UpdateCartItemRequest req) {
        Cart cart = findOrCreateCart(username);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Ítem del carrito", itemId));

        if (item.getProduct().getStock() < req.quantity()) {
            throw new BusinessException("No hay suficiente stock de " + item.getProduct().getName());
        }
        item.setQuantity(req.quantity());
        cartItemRepository.save(item);
        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponse removeItem(String username, UUID itemId) {
        Cart cart = findOrCreateCart(username);
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public void clear(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    Cart findOrCreateCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            Cart newCart = Cart.builder().user(user).build();
            cart = cartRepository.save(newCart);
        }
        return cart;
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream().map(i -> new CartItemResponse(
                i.getId(), i.getProduct().getId(), i.getProduct().getName(),
                i.getProduct().getPrice(), i.getQuantity(),
                i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
        )).toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, total);
    }
}
