package com.example.food_mart.process_test;

import com.example.food_mart.modules.shop.application.CartService;
import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.shop.domain.entity.ItemInCart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/*
    OrderService의 메서드 processOrder() 에 대한 테스트
 */
@ExtendWith(MockitoExtension.class) // Mockito 사용 설정
public class OrderProcessTest {

    @Mock
    private CartService cartService; // CartService를 가짜(Mock) 객체로 생성

    @Test
    void settingCart_test() {
        // given
        Long userId = 1L;
        Cart cart = new Cart();

        given(cartService.selectItemsInCart(userId))
                .willReturn(
                    List.of(
                    new ItemInCart(userId, 2L,"삼겹살", 2, 30000),
                    new ItemInCart(userId,13L,"콜라", 3, 3300)
                ));

        // when
        List<ItemInCart> itemsInCart = cartService.selectItemsInCart(userId);
        cart.setItemsInCart(itemsInCart);
        cart.calculateTotalPrice();
        cart.countItems();

        // then
        assertThat(cart.getItemsInCart()).isEqualTo(itemsInCart);
    }

    @Test
    void judgeBuyable() {

    }

    @Test
    void transactionOrder() {

    }

    @Test
    void transactionMoney() {

    }

    @Test
    void emptyCartAfterOrder() {

    }
}
