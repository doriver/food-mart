package com.example.food_mart.modules.order.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.order.domain.entity.Order;
import com.example.food_mart.modules.order.domain.entity.OrderStatus;
import com.example.food_mart.modules.order.domain.repository.OrderRepository;
import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.warehouse.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final OrderItemService orderItemService;
    private final MoneyService moneyService;
    private final StockService stockService;


    private final OrderRepository orderRepository;


    @Transactional
    public Long order(Long userId, Long deliveryId, Cart cart) {
        // Order생성
        Order order = new Order(userId, deliveryId, OrderStatus.REGISTER, LocalDateTime.now());
        Long savedOrderId = null;
        try {
            savedOrderId = orderRepository.save(order).getId();
        } catch (Exception e) {
            throw new Expected4xxException(ErrorCode.FAIL_ORDER);
        }

        // OrderItem들 저장
        orderItemService.saveOrderItem(savedOrderId, cart.getItemsInCart());

        return savedOrderId;
    }

    /*
        주문 결제
        1.돈 결제    2.Stock 배송 대기상태
     */
    @Transactional
    public void money(Long userId, Cart cart) {
        // 구매자 돈 차감 , 마트 장부에 입금 처리
        moneyService.moneyTransaction(userId, cart.getTotalPrice());

        // 창고에 있는 재고, 배송대기 상태로(out창고로 이동)
        cart.countItems(); // 카트에 담긴 각 itemId와 개수 세팅
        stockService.stockToOutPrepare(cart.getItemAndCountMap());
    }
}
