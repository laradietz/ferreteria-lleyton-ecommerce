package com.ecommerce.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
    @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 80)
    private String username;

    @NotBlank @Email
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String address;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // EAGER a propósito: se lee en el filtro JWT / login fuera de una
    // transacción explícita. Con LAZY acá se rompe con
    // LazyInitializationException (ver notas del proyecto de turnos).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleName role;
}
