package com.example.food_mart.modules.shop.domain.entity;

import com.example.food_mart.modules.user.domain.Wallet;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_wallet", columnList = "wallet_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopLedgerHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Wallet wallet;

    public Long getWalletId() { return wallet.getId(); }

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private ShopTransaction shopTransaction;

    private long amount;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public ShopLedgerHistory(Wallet wallet, ShopTransaction shopTransaction, long amount, LocalDateTime createdAt) {
        this.wallet = wallet;
        this.shopTransaction = shopTransaction;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}
