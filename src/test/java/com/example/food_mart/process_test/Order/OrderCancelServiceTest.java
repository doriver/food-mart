package com.example.food_mart.process_test.Order;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.order.application.OrderService;
import com.example.food_mart.modules.order.application.TransactionService;
import com.example.food_mart.modules.order.domain.entity.Order;
import com.example.food_mart.modules.order.domain.entity.OrderStatus;
import com.example.food_mart.modules.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCancelServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private OrderService orderService;

    private Order createOrder(Long id, Long userId, OrderStatus status) {
        Order order = new Order(userId, "서울시 강남구", status);
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    @Test
    @DisplayName("WAITDELIVERY 상태 주문 취소 성공")
    void cancelOrder_waitDelivery_success() {
        // given
        Long orderId = 1L;
        Long userId = 10L;
        String reason = "단순 변심";
        Order order = createOrder(orderId, userId, OrderStatus.WAITDELIVERY);

        given(orderRepository.findByIdAndUserId(orderId, userId))
                .willReturn(Optional.of(order));

        // when
        orderService.cancelOrder(orderId, userId, reason);

        // then
        verify(transactionService).cancelOrder(order, reason);
    }

    @Test
    @DisplayName("REGISTER 상태 주문 취소 성공")
    void cancelOrder_register_success() {
        // given
        Long orderId = 2L;
        Long userId = 10L;
        String reason = "주문 실수";
        Order order = createOrder(orderId, userId, OrderStatus.REGISTER);

        given(orderRepository.findByIdAndUserId(orderId, userId))
                .willReturn(Optional.of(order));

        // when
        orderService.cancelOrder(orderId, userId, reason);

        // then
        verify(transactionService).cancelOrder(order, reason);
    }

    @Test
    @DisplayName("DELIVERY 상태 주문 취소 실패")
    void cancelOrder_delivery_fail() {
        // given
        Long orderId = 3L;
        Long userId = 10L;
        Order order = createOrder(orderId, userId, OrderStatus.DELIVERY);

        given(orderRepository.findByIdAndUserId(orderId, userId))
                .willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId, "취소 원함"))
                .isInstanceOf(Expected4xxException.class)
                .hasMessageContaining(ErrorCode.NOT_CANCELLABLE_STATUS.getMessage());

        verify(transactionService, never()).cancelOrder(any(), any());
    }

    @Test
    @DisplayName("이미 취소된 주문 취소 실패")
    void cancelOrder_alreadyCancelled_fail() {
        // given
        Long orderId = 4L;
        Long userId = 10L;
        Order order = createOrder(orderId, userId, OrderStatus.CANCEL);

        given(orderRepository.findByIdAndUserId(orderId, userId))
                .willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId, "다시 취소"))
                .isInstanceOf(Expected4xxException.class)
                .hasMessageContaining(ErrorCode.ALREADY_CANCELLED.getMessage());

        verify(transactionService, never()).cancelOrder(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 주문 취소 실패")
    void cancelOrder_notFound_fail() {
        // given
        Long orderId = 999L;
        Long userId = 10L;

        given(orderRepository.findByIdAndUserId(orderId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId, "취소"))
                .isInstanceOf(Expected4xxException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_ORDER.getMessage());

        verify(transactionService, never()).cancelOrder(any(), any());
    }
}
