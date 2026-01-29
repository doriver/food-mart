package com.example.food_mart.modules.user.domain;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_user", columnList = "userId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    private long money;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Wallet(Long userId, long money, LocalDateTime updatedAt) {
        this.userId = userId;
        this.money = money;
        this.updatedAt = updatedAt;
    }

    public void minusMoney(long cost) {
        if (money > cost) {
            this.money = money - cost;
        } else { // 이건 여기서 해주는게 좋을듯?
            throw new Expected4xxException(ErrorCode.NOT_ENOUGH_MONEY);
        }
    }
}
