package com.example.food_mart.process_test.Order;

import com.example.food_mart.modules.order.application.LedgerPaymentService;
import com.example.food_mart.modules.shop.domain.entity.ShopLedgerHistory;
import com.example.food_mart.modules.shop.domain.repository.ShopLedgerHistoryRepository;
import com.example.food_mart.modules.user.domain.Wallet;
import com.example.food_mart.modules.user.domain.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class LedgerPaymentServiceTest {

    @InjectMocks
    private LedgerPaymentService ledgerPaymentService;

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private ShopLedgerHistoryRepository shopLedgerHistoryRepository;

    @Test
    @DisplayName("주문 결제에서, 구매자 돈 차감 + 마트 장부에 입금 처리")
    void moneyTransaction_test() {
        // given
        Long userId = 4L;
        Long totalPrice = 33300L;

        Wallet wallet = new Wallet(userId, 50000L);
        ReflectionTestUtils.setField(wallet, "id", 4L);

        given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));
        given(walletRepository.save(any(Wallet.class)))
                .willAnswer(invocation -> {
                    Wallet walletAfterMinus = invocation.getArgument(0);  // save에 들어온 객체
                    return walletAfterMinus;
                });
        given(shopLedgerHistoryRepository.save(any(ShopLedgerHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        ledgerPaymentService.moneyTransaction(userId,totalPrice);

        // then
        assertThat(wallet.getMoney()).isEqualTo(50000L - totalPrice);
    }
}
