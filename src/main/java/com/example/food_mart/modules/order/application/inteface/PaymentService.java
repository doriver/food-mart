package com.example.food_mart.modules.order.application.inteface;

public interface PaymentService {
    void moneyTransaction(Long userId, Long totalPrice);
}
