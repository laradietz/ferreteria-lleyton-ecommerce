package com.ecommerce.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Order extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @NotBlank
    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY,
              cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @JsonIgnore
    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY,
              cascade = CascadeType.ALL, orphanRemoval = true)
    private Shipment shipment;
}
