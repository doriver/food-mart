package com.example.food_mart.modules.order.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.shop.domain.entity.ShopLedgerHistory;
import com.example.food_mart.modules.shop.domain.entity.ShopTransaction;
import com.example.food_mart.modules.shop.domain.repository.ShopLedgerHistoryRepository;
import com.example.food_mart.modules.user.domain.Wallet;
import com.example.food_mart.modules.user.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MoneyService {

    private final WalletRepository walletRepository;
    private final ShopLedgerHistoryRepository shopLedgerHistoryRepository;

    /*
        구매자 돈 차감 , 마트 장부에 입금 처리
        1. 구매자 지갑 조회해서, 차감하기
        2. 마트 장부에 반영하기
     */
    public void moneyTransaction(Long userId, Long totalPrice) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new Expected4xxException(ErrorCode.WALLET_NOT_FOUND));
        wallet.minusMoney(totalPrice);

        ShopLedgerHistory shopLedgerHistory = new ShopLedgerHistory(wallet.getId(), ShopTransaction.PAY, totalPrice, LocalDateTime.now());

        try {
            walletRepository.save(wallet);
            shopLedgerHistoryRepository.save(shopLedgerHistory);
        } catch (Exception e) {
            throw new Expected4xxException(ErrorCode.FAIL_TRANSACTION);
        }
    }
}
