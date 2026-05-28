package com.example.food_mart.modules.order.domain.entity;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_status_updated_at", columnList = "status, updated_at") // 출고 지연 탐지에서 추가
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    public Long getUserId() { return user.getId(); }

    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OrderStatus status;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false // DB가 직접 입력하므로 ,JPA는 신경 끄라는 의미
    )
    private LocalDateTime updatedAt;

    public Order(User user, String deliveryAddress, OrderStatus status) {
        this.user = user;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new Expected4xxException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = newStatus;
    }
}
