package com.example.food_mart.process_test.Order;

import com.example.food_mart.modules.shop.application.CartService;
import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.shop.domain.entity.ItemInCart;
import com.example.food_mart.modules.shop.domain.repository.ItemInCartRepository;
import com.example.food_mart.modules.user.application.WalletReadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

/*
    OrderService의 메서드 processOrder() 에 대한 테스트
 */
@ExtendWith(MockitoExtension.class) // Mockito 사용 설정
public class CartServiceTest {

    @Spy
    @InjectMocks
    private CartService cartService; // CartService를 가짜(Mock) 객체로 생성


    @Mock
    private WalletReadService walletReadService;

    @Test
    @DisplayName("도메인 객체 생성후 세팅")
    void settingCart_test() {
        // given
        Long userId = 1L;
        Cart cart = new Cart();

        ItemInCart itemInCart01 = new ItemInCart(userId, 2L,"삼겹살", 2, 30000);
        ItemInCart itemInCart02 = new ItemInCart(userId,13L,"콜라", 3, 3300);
        doReturn(List.of(itemInCart01, itemInCart02))
                .when(cartService).selectItemsInCart(userId);

        // when
        cartService.settingCart(userId, cart);

        // then
        assertThat(cart.getItemsInCart()).isEqualTo(List.of(itemInCart01, itemInCart02));
        assertThat(cart.getTotalPrice()).isEqualTo(30000 + 3300);
        assertThat(cart.getItemAndCountMap()).isEqualTo(Map.of(2L, 2, 13L, 3));
    }

    @Test
    @DisplayName("구매할수 있는지 판단(가격)")
    void judgeBuyableMoney_test() {
        // given
        Long userId = 1L;
        Cart cart = new Cart();
        cart.setItemsInCart(List.of(
                new ItemInCart(userId, 2L,"삼겹살", 2, 30000),
                new ItemInCart(userId,13L,"콜라", 3, 3300)
        ));
        cart.calculateTotalPrice();

        given(walletReadService.selectMoney(userId)).willReturn(50000l);

        // when
        long userMoney = walletReadService.selectMoney(userId);

        // then
        assertThat(userMoney >= cart.getTotalPrice()).isTrue();
    }

    @Test
    @DisplayName("구매할수 있는지 판단(재고)")
    void judgeBuyableCount_test() {
        // given
        List<ItemInCart> ItemsInCart = List.of(
            new ItemInCart(1L, 2L,"삼겹살", 2, 30000),
            new ItemInCart(1L,13L,"콜라", 3, 3300)
        );

        // when

        // then

    }

    @Test
    void transactionOrder() {
        // given

        // when

        // then
    }

    @Test
    void transactionMoney() {
        // given

        // when

        // then
    }

    @Test
    void emptyCartAfterOrder() {
        // given

        // when

        // then
    }
}
