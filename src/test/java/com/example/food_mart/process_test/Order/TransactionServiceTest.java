package com.example.food_mart.process_test.Order;

import com.example.food_mart.modules.order.application.OrderItemService;
import com.example.food_mart.modules.order.application.TransactionService;
import com.example.food_mart.modules.order.application.inteface.PaymentService;
import com.example.food_mart.modules.order.domain.entity.Order;
import com.example.food_mart.modules.order.domain.entity.OrderStatus;
import com.example.food_mart.modules.order.domain.repository.OrderRepository;
import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.shop.domain.entity.ItemInCart;
import com.example.food_mart.modules.warehouse.application.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private OrderItemService orderItemService;
    @Mock
    private PaymentService ledgerPaymentService;
    @Mock
    private StockService stockService;
    @Mock
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 생성")
    void order_test() {
        // given
        Long userId = 2L;
        String deliveryAddress = "강남구 1번지";
        Cart cart = new Cart();
        cart.setItemsInCart(List.of(
                new ItemInCart(userId, 2L,"삼겹살", 2, 30000),
                new ItemInCart(userId,13L,"콜라", 3, 3300)
        ));

        Order savedOrder = new Order(userId, deliveryAddress, OrderStatus.REGISTER);
        ReflectionTestUtils.setField(savedOrder, "id", 5L);
        given(orderRepository.save(any(Order.class)))
                .willReturn(savedOrder);
        willDoNothing().given(orderItemService)
                .saveOrderItem(savedOrder.getId(), cart.getItemsInCart());

        // when & then
        assertThat(transactionService.order(userId, deliveryAddress, cart))
                .isEqualTo(savedOrder);
    }

    @Test
    @DisplayName("주문 결제") // 하다 말음, 이어서 해야함
    void money_test() {
        // given
        Long userId = 2L;
        Cart cart = new Cart();
        cart.setItemsInCart(List.of(
                new ItemInCart(userId, 2L,"삼겹살", 2, 30000),
                new ItemInCart(userId,13L,"콜라", 3, 3300)
        ));

        Order order = new Order(userId, "강남구 1번지", OrderStatus.REGISTER);
        ReflectionTestUtils.setField(order, "id", 5L);

        willDoNothing().given(orderRepository)
                .save(any(Order.class));


        // when
        transactionService.money(userId, cart, order);

        // then


        assertThat(order.getStatus()).isEqualTo(OrderStatus.WAITDELIVERY);

    }

}
