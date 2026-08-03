package com.ecommerce.api.service;

import com.ecommerce.api.dto.AddCartItemRequest;
import com.ecommerce.api.entity.*;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductService productService;
    @InjectMocks private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).username("cliente1")
                .role(RoleName.CUSTOMER).active(true).build();
        cart = Cart.builder().id(UUID.randomUUID()).user(user).items(new ArrayList<>()).build();
        product = Product.builder().id(UUID.randomUUID()).sku("HM-0001").name("Martillo")
                .price(new BigDecimal("8500")).stock(5).active(true).build();
    }

    @Test
    void addItem_lanzaBusinessException_siNoHaySuficienteStock() {
        var request = new AddCartItemRequest(product.getId(), 10);

        when(userRepository.findByUsername("cliente1")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productService.findEntity(product.getId())).thenReturn(product);

        assertThatThrownBy(() -> cartService.addItem("cliente1", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("stock");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addItem_agregaElItem_cuandoHayStockSuficiente() {
        var request = new AddCartItemRequest(product.getId(), 2);

        when(userRepository.findByUsername("cliente1")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productService.findEntity(product.getId())).thenReturn(product);
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
                .thenReturn(Optional.empty());
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

        var response = cartService.addItem("cliente1", request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.total()).isEqualByComparingTo("17000");
        verify(cartItemRepository).save(any());
    }
}
