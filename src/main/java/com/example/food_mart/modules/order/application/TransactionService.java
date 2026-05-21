package com.example.food_mart.modules.order.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected5xxException;
import com.example.food_mart.modules.logistic.application.OutboundService;
import com.example.food_mart.modules.order.application.inteface.PaymentService;
import com.example.food_mart.modules.order.domain.entity.Order;
import com.example.food_mart.modules.user.domain.User;
import com.example.food_mart.modules.user.domain.UserRepository;
import com.example.food_mart.modules.order.domain.entity.OrderHistory;
import com.example.food_mart.modules.order.domain.entity.OrderItem;
import com.example.food_mart.modules.order.domain.entity.OrderStatus;
import com.example.food_mart.modules.order.domain.repository.OrderHistoryRepository;
import com.example.food_mart.modules.order.domain.repository.OrderItemRepository;
import com.example.food_mart.modules.order.domain.repository.OrderRepository;
import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.warehouse.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final OrderItemService orderItemService;
    private final PaymentService ledgerPaymentService;
    private final StockService stockService;
    private final OutboundService outboundService;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderHistoryRepository orderHistoryRepository;


    @Transactional
    public Order order(Long userId, String deliveryAddress, Cart cart) {
        // Order생성
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

    /*
        주문 결제
        1.돈 결제    2.Stock 배송 대기상태
     */
    @Transactional
    public void money(Long userId, Cart cart, Order order) {
        // 구매자 돈 차감 , 마트 장부에 입금 처리
        ledgerPaymentService.moneyTransaction(userId, cart.getTotalPrice());

        // 창고에 있는 재고, 배송대기 상태로
        stockService.stockToOutPrepare(cart.getItemAndCountMap(), order.getId());

        // 주문 상태 업데이트
        order.updateStatus(OrderStatus.WAITDELIVERY);
        orderRepository.save(order);
    }

    /*
        주문 취소 처리
        1. WAITDELIVERY 상태인 경우: 출고취소 + 환불 + 재고복원
        2. 주문 상태 CANCEL로 변경
        3. 주문 이력 기록
     */
    @Transactional
    public void cancelOrder(Order order, String reason) {
        OrderStatus previousStatus = order.getStatus();

        // 결제 완료 상태인 경우에만 환불 및 재고 복원
        if (previousStatus == OrderStatus.WAITDELIVERY) {
            // 출고 취소
            outboundService.cancelOutboundIfExists(order.getId());

            // 환불 금액 계산
            List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());
            long totalRefund = orderItems.stream()
                    .mapToLong(OrderItem::getTotalPrice)
                    .sum();

            // 환불 처리
            ledgerPaymentService.refundTransaction(order.getUserId(), totalRefund);

            // 재고 복원
            stockService.restoreStockFromPickings(order.getId());
        }

        // 주문 상태 변경
        order.updateStatus(OrderStatus.CANCEL);
        orderRepository.save(order);

        // 주문 이력 기록
        OrderHistory orderHistory = new OrderHistory(
                order, previousStatus, OrderStatus.CANCEL, reason);
        orderHistoryRepository.save(orderHistory);
    }
}
