package com.example.food_mart.modules.logistic.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_staff", columnList = "staffId")
        , @Index(name = "idx_delivery", columnList = "deliveryId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long staffId; // 출고 처리 담당자(특정 권한 필요)

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

    public Outbound(Long staffId, Long deliveryId, OutboundStatus outboundStatus) {
        this.staffId = staffId;
        this.deliveryId = deliveryId;
        this.outboundStatus = outboundStatus;
    }
}
