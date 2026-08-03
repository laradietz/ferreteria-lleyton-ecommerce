package com.ecommerce.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notifications")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Notification extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, length = 500)
    private String body;

    @Builder.Default
    @Column(nullable = false)
    private Boolean read = false;
}
