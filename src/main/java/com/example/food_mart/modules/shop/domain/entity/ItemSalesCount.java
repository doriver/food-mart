package com.example.food_mart.modules.shop.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_sales_category", columnList = "category_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemSalesCount {

    @Id
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Category category;

    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }

    // totalCount, weeklyCount는 JPA 기본값인 0으로 초기화됨
    private long totalCount;

    private long weeklyCount;

    private LocalDateTime syncedAt;

    public ItemSalesCount(Long itemId, Category category) {
        this.itemId = itemId;
        this.category = category;
        this.syncedAt = LocalDateTime.now();
    }

    public void updateWeeklyCount(long weeklyCount) {
        this.weeklyCount = weeklyCount;
        this.syncedAt = LocalDateTime.now();
    }

    public void addTotalCount(long amount) {
        this.totalCount += amount;
    }
}
