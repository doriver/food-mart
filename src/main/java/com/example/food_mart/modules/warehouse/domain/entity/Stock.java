package com.example.food_mart.modules.warehouse.domain.entity;

import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.shop.domain.entity.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_item", columnList = "item_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long count;

    @NotNull
    private WarehousePurpose locationType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Item item;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Warehouse warehouse;

    public Long getItemId() { return item.getId(); }
    public Long getWarehouseId() { return warehouse.getId(); }

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false // DB가 직접 입력하므로 ,JPA는 신경 끄라는 의미
    )
    private LocalDateTime updatedAt;

    public Stock(Long count, WarehousePurpose locationType, Item item, Warehouse warehouse) {
        this.count = count;
        this.locationType = locationType;
        this.item = item;
        this.warehouse = warehouse;
    }

    // 일반적인 개수 감소는 아님, 주문에서 사용되는 개수 감소
    public long minusCount(long mc) {
        if (mc <= count) {
            this.count = count - mc;
            return 0; // 추가작업 필요없음
        } else {
            // mc > count 인 경우
            this.count = 0;
            return mc - count; // 남는거 만큼 다른 재고에서 까야함
        }
    }

    public void plusCount(long pc) {
        this.count = count + pc;
    }
}
