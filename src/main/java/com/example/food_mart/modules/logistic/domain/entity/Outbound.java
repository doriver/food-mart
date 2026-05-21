package com.example.food_mart.modules.logistic.domain.entity;

import com.example.food_mart.modules.staff.domain.Staff;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_staff", columnList = "staff_id")
        , @Index(name = "idx_delivery", columnList = "deliveryId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Staff staff; // 출고 처리 담당자(특정 권한 필요)

    public Long getStaffId() { return staff.getId(); }

    @NotNull
    private Long deliveryId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OutboundStatus outboundStatus;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false // DB가 직접 입력하므로 ,JPA는 신경 끄라는 의미
    )
    private LocalDateTime updatedAt;

    public Outbound(Staff staff, Long deliveryId, OutboundStatus outboundStatus) {
        this.staff = staff;
        this.deliveryId = deliveryId;
        this.outboundStatus = outboundStatus;
    }

    public void updateOutboundStatus(OutboundStatus outboundStatus) {
        this.outboundStatus = outboundStatus;
    }

    public void completeBy(Staff staff) {
        this.outboundStatus = OutboundStatus.COMPLETED;
        this.staff = staff;
    }
}
