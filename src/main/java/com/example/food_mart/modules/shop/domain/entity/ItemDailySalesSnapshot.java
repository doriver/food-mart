package com.example.food_mart.modules.shop.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "item_daily_sales_snapshot",
        indexes = {
                @Index(name = "idx_sales_date", columnList = "sales_date"),
                @Index(name = "idx_sales_date_item", columnList = "sales_date, item_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemDailySalesSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Long salesCount;

    @Column(name = "sales_date", nullable = false)
    private LocalDate salesDate;

    @Column(nullable = false)
    private LocalDateTime snapshotAt;

    public ItemDailySalesSnapshot(Long itemId, String itemName, Long salesCount,
                                  LocalDate salesDate, LocalDateTime snapshotAt) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.salesCount = salesCount;
        this.salesDate = salesDate;
        this.snapshotAt = snapshotAt;
    }
}
