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
        @Index(name = "idx_order", columnList = "orderId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long orderId;

    @NotBlank
    private String address; // order에 있어서 빼는게 나을수도

    @Enumerated(EnumType.STRING)
    @NotNull
    private DeliveryCompany deliveryCompany; // 택배사

    @NotBlank
    private String trackingCode; // 송장 번호

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false // DB가 직접 입력하므로 ,JPA는 신경 끄라는 의미
    )
    private LocalDateTime updatedAt;

    public Delivery(Long orderId, String address, DeliveryCompany deliveryCompany, String trackingCode) {
        this.orderId = orderId;
        this.address = address;
        this.deliveryCompany = deliveryCompany;
        this.trackingCode = trackingCode;
    }
}
