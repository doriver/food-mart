package com.example.food_mart.modules.warehouse.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_staff", columnList = "staffId")
        , @Index(name = "idx_order", columnList = "orderId")
        , @Index(name = "idx_stock", columnList = "stockId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Picking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long orderId;

    @NotNull
    private Long staffId;

    @NotNull
    private Long stockId;

    private long count;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PickingStatus pickingStatus;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false // DB가 직접 입력하므로 ,JPA는 신경 끄라는 의미
    )
    private LocalDateTime updatedAt;

    public Picking(Long orderId, Long stockId, long count, PickingStatus pickingStatus, Long staffId) {
        this.orderId = orderId;
        this.stockId = stockId;
        this.count = count;
        this.pickingStatus = pickingStatus;
        this.staffId = staffId;
    }
}
