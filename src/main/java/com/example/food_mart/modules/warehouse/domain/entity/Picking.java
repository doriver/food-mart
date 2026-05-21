package com.example.food_mart.modules.warehouse.domain.entity;

import com.example.food_mart.modules.order.domain.entity.Order;
import com.example.food_mart.modules.staff.domain.Staff;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_staff", columnList = "staff_id")
        , @Index(name = "idx_order", columnList = "order_id")
        , @Index(name = "idx_stock", columnList = "stock_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Picking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Staff staff;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Stock stock;

    public Long getOrderId() { return order.getId(); }
    public Long getStaffId() { return staff.getId(); }
    public Long getStockId() { return stock.getId(); }

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

    public Picking(Order order, Stock stock, long count, PickingStatus pickingStatus, Staff staff) {
        this.order = order;
        this.stock = stock;
        this.count = count;
        this.pickingStatus = pickingStatus;
        this.staff = staff;
    }

    public void completeBy(Staff staff) {
        this.pickingStatus = PickingStatus.COMPLETED;
        this.staff = staff;
    }
}
