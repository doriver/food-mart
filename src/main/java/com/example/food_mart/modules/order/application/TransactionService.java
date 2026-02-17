package com.example.food_mart.modules.order.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected5xxException;
import com.example.food_mart.modules.order.application.inteface.PaymentService;
import com.example.food_mart.modules.order.domain.entity.Order;
import com.example.food_mart.modules.order.domain.entity.OrderStatus;
import com.example.food_mart.modules.order.domain.repository.OrderRepository;
import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.warehouse.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final OrderItemService orderItemService;
    private final PaymentService ledgerPaymentService;
    private final StockService stockService;


    private final OrderRepository orderRepository;


    @Transactional
    public Order order(Long userId, String deliveryAddress, Cart cart) {
        // Order생성
        Order order = new Order(userId, deliveryAddress, OrderStatus.REGISTER);

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
}
