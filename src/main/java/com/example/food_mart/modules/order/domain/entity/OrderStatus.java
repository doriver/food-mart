package com.example.food_mart.modules.order.domain.entity;

public enum OrderStatus {
    REGISTER
    , PAID // 결제완료( 새로 추가함, 기존엔 WAITDELIVERY로 했었음, 이에따른 코드수정 필요 )
    , WAITDELIVERY // 배송 대기상태로 완료
    , DELIVERY // 배송중(=출고 완료)
    , COMPLETE, CANCEL;
}
