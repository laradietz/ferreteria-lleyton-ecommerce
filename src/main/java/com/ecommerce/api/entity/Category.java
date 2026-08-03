package com.ecommerce.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "categories", uniqueConstraints =
    @UniqueConstraint(name = "uk_category_name", columnNames = "name"))
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Category extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
