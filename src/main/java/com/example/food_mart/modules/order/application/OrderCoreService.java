package com.example.food_mart.modules.order.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected5xxException;
import com.example.food_mart.modules.order.domain.entity.Order;
import com.example.food_mart.modules.order.domain.entity.OrderHistory;
import com.example.food_mart.modules.order.domain.entity.OrderStatus;
import com.example.food_mart.modules.order.domain.repository.OrderHistoryRepository;
import com.example.food_mart.modules.order.domain.repository.OrderRepository;
import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.user.domain.User;
import com.example.food_mart.modules.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCoreService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final OrderHistoryRepository orderHistoryRepository;

    // 주문 생성
    @Transactional
    public Order createOrder(Long userId, String deliveryAddress, Cart cart) {
        User user = userRepository.getReferenceById(userId);
        Order order = new Order(user, deliveryAddress, OrderStatus.REGISTER);

        Order savedOrder = null;
        try {
            savedOrder = orderRepository.save(order);
        } catch (Exception e) {
            throw new Expected5xxException(ErrorCode.FAIL_ORDER);
        }

        // OrderItem들 저장
        orderItemService.saveOrderItem(savedOrder.getId(), cart.getItemsInCart());
        return savedOrder;
    }

    // 주문 상태 변경( 상태변경 + 변경이력저장 )
    @Transactional
    public void updateOrderStatus(Order order, OrderStatus changedStatus, String reason) {
        OrderHistory orderHistory = new OrderHistory(
                order, order.getStatus(), changedStatus, reason);
        order.updateStatus(changedStatus);

        orderRepository.save(order);
        orderHistoryRepository.save(orderHistory);
    }
}
