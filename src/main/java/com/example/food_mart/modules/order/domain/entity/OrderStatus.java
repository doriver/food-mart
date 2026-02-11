package com.example.food_mart.modules.order.domain.entity;

public enum OrderStatus {
    REGISTER
    , WAITDELIVERY // (=결제 완료)
    , DELIVERY // 배송중
    , COMPLETE, CANCEL;
}
