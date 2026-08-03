package com.ecommerce.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "products", uniqueConstraints =
    @UniqueConstraint(name = "uk_product_sku", columnNames = "sku"))
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Product extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 40)
    private String sku;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull @DecimalMin("0.0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull @Min(0)
    @Column(nullable = false)
    private Integer stock;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // EAGER: se necesita en casi toda respuesta de producto (nombre de
    // categoría), y evita LazyInitializationException con open-in-view=false.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
