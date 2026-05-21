package com.example.food_mart.modules.logistic.domain.entity;

import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.staff.domain.Staff;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_inbound", columnList = "inbound_id"),
        @Index(name = "idx_staff", columnList = "stacking_staff_id"),
        @Index(name = "idx_item", columnList = "item_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboundItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Inbound inbound;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Item item;

    @NotNull
    private Long count;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stacking_staff_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Staff stackingStaff;

    public Long getInboundId() { return inbound.getId(); }
    public Long getItemId() { return item.getId(); }
    public Long getStackingStaffId() { return stackingStaff.getId(); }

    @Enumerated(EnumType.STRING)
    @NotNull
    private InboundStackingStatus inboundStackingStatus;

    // 유통기한 등 다른 정보들 올수 있음

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false // DB가 직접 입력하므로 ,JPA는 신경 끄라는 의미
    )
    private LocalDateTime updatedAt;

    public InboundItem(Inbound inbound, Item item, Long count, Staff stackingStaff, InboundStackingStatus inboundStackingStatus) {
        this.inbound = inbound;
        this.item = item;
        this.count = count;
        this.stackingStaff = stackingStaff;
        this.inboundStackingStatus = inboundStackingStatus;
    }

    public void updateStackingStaff(Staff staff) {
        this.stackingStaff = staff;
    }

    public void updateInboundStackingStatus(InboundStackingStatus inboundStackingStatus) {
        this.inboundStackingStatus = inboundStackingStatus;
    }
}
