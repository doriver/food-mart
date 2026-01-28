package com.example.food_mart.modules.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_user", columnList = "userId"),
        @Index(name = "idx_delivery", columnList = "deliveryId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private Long deliveryId;

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private OrderStatus status;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public Order(Long userId, Long deliveryId, OrderStatus status, LocalDateTime createdAt) {
        this.userId = userId;
        this.deliveryId = deliveryId;
        this.status = status;
        this.createdAt = createdAt;
    }



}
