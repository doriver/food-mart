package com.example.food_mart.modules.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/*
    특이사항 있는 변화에대해 기록할 예정
 */
@Entity
@Table(indexes = {
        @Index(name = "idx_order", columnList = "orderId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OrderStatus changedStatus;

    private String reason; // 기본값으로 0같은거 넣어도 될듯

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false
    )
    private LocalDateTime updatedAt;

    public OrderHistory(Long orderId, OrderStatus previousStatus, OrderStatus changedStatus, String reason) {
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.changedStatus = changedStatus;
        this.reason = reason;
    }
}
