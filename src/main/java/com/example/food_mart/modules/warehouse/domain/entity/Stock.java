package com.example.food_mart.modules.warehouse.domain.entity;

import com.example.food_mart.common.exception.Expected4xxException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_warehouse", columnList = "warehouseId"),
        @Index(name = "idx_item", columnList = "itemId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long count;

    @NotNull
    private WarehousePurpose locationType;

    @NotNull
    private Long itemId;

    @NotNull
    private Long warehouseId;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false // DB가 직접 입력하므로 ,JPA는 신경 끄라는 의미
    )
    private LocalDateTime updatedAt;

    public Stock(Long count, Long itemId, Long warehouseId) {
        this.count = count;
        this.itemId = itemId;
        this.warehouseId = warehouseId;
    }

    public void minusCount(int mc) {
        if (mc <= count) {
            this.count = count - mc;
        } else {
            throw new Expected4xxException("개수가 부족합니다.");
        }
    }

    public void plusCount(int pc) {
        this.count = count + pc;
    }
}
