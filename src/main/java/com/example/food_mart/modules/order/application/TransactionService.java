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

    private final OrderCoreService orderCoreService;
    private final PaymentService ledgerPaymentService;
    private final StockService stockService;
    private final OutboundService outboundService;
    private final OrderItemRepository orderItemRepository;

    /*
        주문 결제
        1.돈 결제    2.Stock 배송 대기상태
     */
    // 일부러 @Transactional 제거, 각 세부 메서드에 걸려있음
    public void money(Long userId, Cart cart, Order order) {
        // 구매자 돈 차감 , 마트 장부에 입금 처리
        ledgerPaymentService.moneyTransaction(userId, cart.getTotalPrice(), order);

        // 창고에 있는 재고, 배송대기 상태로
        stockService.stockToOutPrepare(cart.getItemAndCountMap(), order);
    }

    /*
        주문취소_환불
     */
    @Transactional
    public void cancelPaid(Order order, String reason) {
        refund(order);
        orderCoreService.updateOrderStatus(order, OrderStatus.CANCEL, reason);
    }

    public void refund(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());
        long totalRefund = orderItems.stream()
                .mapToLong(OrderItem::getTotalPrice)
                .sum();
        ledgerPaymentService.refundTransaction(order.getUserId(), totalRefund);
    }

    /*
        배송대기 취소 : 출고취소 + 재고복원
        @Transactional 제거: 재고 pessimistic lock 보유 시간 최소화
     */
    public void cancelWaitDelivery(Order order) {
        outboundService.cancelOutboundIfExists(order.getId()); // 멱등성
        stockService.restoreStockFromPickings(order.getId()); // 멱등성 , pessimistic lock
    }
}
