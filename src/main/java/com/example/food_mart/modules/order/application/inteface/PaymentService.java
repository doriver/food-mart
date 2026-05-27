package com.example.food_mart.modules.order.application.inteface;

import com.example.food_mart.modules.order.domain.entity.Order;

public interface PaymentService {
    void moneyTransaction(Long userId, Long totalPrice, Order order);
    void refundTransaction(Long userId, Long totalPrice);
}
