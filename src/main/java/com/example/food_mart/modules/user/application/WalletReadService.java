package com.example.food_mart.modules.user.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.user.domain.Wallet;
import com.example.food_mart.modules.user.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletReadService {
    private final WalletRepository walletRepository;

    /*
        특정 사용자가 가진 돈 조회
     */
    public long selectMoney(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new Expected4xxException(ErrorCode.WALLET_NOT_FOUND));

        return wallet.getMoney();
    }
}
