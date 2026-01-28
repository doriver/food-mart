package com.example.food_mart.modules.shop.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_wallet", columnList = "walletId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopLedgerHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long walletId;

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private ShopTransaction shopTransaction;

    private long amount;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public ShopLedgerHistory(Long walletId, ShopTransaction shopTransaction, long amount, LocalDateTime createdAt) {
        this.walletId = walletId;
        this.shopTransaction = shopTransaction;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}
