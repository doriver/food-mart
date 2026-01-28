package com.example.food_mart.modules.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private DeliveryStatus status;

    private String address;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public Delivery(DeliveryStatus status, String address, LocalDateTime createdAt) {
        this.status = status;
        this.address = address;
        this.createdAt = createdAt;
    }
}
