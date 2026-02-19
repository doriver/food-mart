package com.example.food_mart.modules.logistic.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_inbound", columnList = "inboundId"),
        @Index(name = "idx_staff", columnList = "stackingStaffId"),
        @Index(name = "idx_item", columnList = "itemId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboundItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long inboundId; // 입고

    @NotNull
    private Long itemId; // 아이템

    @NotNull
    private Long count;

    @NotNull
    private Long stackingStaffId;

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

    public InboundItem(Long inboundId, Long itemId, Long count, Long stackingStaffId, InboundStackingStatus inboundStackingStatus) {
        this.inboundId = inboundId;
        this.itemId = itemId;
        this.count = count;
        this.stackingStaffId = stackingStaffId;
        this.inboundStackingStatus = inboundStackingStatus;
    }

    public void updateStackingStaffId(Long stackingStaffId) {
        this.stackingStaffId = stackingStaffId;
    }

    public void updateInboundStackingStatus(InboundStackingStatus inboundStackingStatus) {
        this.inboundStackingStatus = inboundStackingStatus;
    }
}
