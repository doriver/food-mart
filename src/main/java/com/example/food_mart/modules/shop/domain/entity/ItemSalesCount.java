package com.example.food_mart.modules.shop.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_sales_category", columnList = "categoryId"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemSalesCount {

    @Id
    private Long itemId;

    private Long categoryId;

    private long totalCount;

    private long weeklyCount;

    private LocalDateTime syncedAt;

    public ItemSalesCount(Long itemId, Long categoryId, long totalCount, long weeklyCount) {
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.totalCount = totalCount;
        this.weeklyCount = weeklyCount;
        this.syncedAt = LocalDateTime.now();
    }

    public void update(Long categoryId, long totalCount, long weeklyCount) {
        this.categoryId = categoryId;
        this.totalCount = totalCount;
        this.weeklyCount = weeklyCount;
        this.syncedAt = LocalDateTime.now();
    }
}
