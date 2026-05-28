package com.example.food_mart.modules.order.domain.entity;

import java.util.*;

public enum OrderStatus {
    REGISTER, PAID // 결제완료( 새로 추가함, 기존엔 WAITDELIVERY로 했었음, 이에따른 코드수정 필요 )
    , WAITDELIVERY, DELIVERY, COMPLETE // 배송 대기상태로 , 배송중(=출고 완료)
    , REFUND, CANCEL, ERROR;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            REGISTER,     Set.of(PAID, CANCEL),
            PAID,         Set.of(WAITDELIVERY, CANCEL),
            WAITDELIVERY, Set.of(DELIVERY, CANCEL),
            DELIVERY,     Set.of(COMPLETE, ERROR),
            COMPLETE,     Set.of(REFUND),
            REFUND,       Set.of(),
            CANCEL,       Set.of(),
            ERROR,        Set.of()
    );

    public boolean canTransitionTo(OrderStatus next) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(next);
    }
}
