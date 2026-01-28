package com.example.food_mart.modules.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_order", columnList = "orderId"),
        @Index(name = "idx_item", columnList = "itemId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long orderId;

    @NotNull
    private Long itemId;

    private String name;

    private int count;

    private long totalPrice;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public OrderItem(Long orderId, Long itemId, String name, int count, long totalPrice, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.name = name;
        this.count = count;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }
}
