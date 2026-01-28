package com.example.food_mart.modules.shop.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
    cart는 redis로 할수도 있음
 */
@Entity
@Table(indexes = {
        @Index(name = "idx_user", columnList = "userId"),
        @Index(name = "idx_item", columnList = "itemId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemInCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private Long itemId;

    private String name;

    private int count;

    private long totalPrice;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public ItemInCart(Long userId, Long itemId, String name, int count, long totalPrice, LocalDateTime createdAt) {
        this.userId = userId;
        this.itemId = itemId;
        this.name = name;
        this.count = count;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }
}
