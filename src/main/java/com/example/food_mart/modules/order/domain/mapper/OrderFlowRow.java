package com.example.food_mart.modules.order.domain.mapper;

public record OrderFlowRow(
        Long orderId,
        String orderStatus,
        Integer totalPicking,
        Integer completedPicking,
        String outboundStatus,
        String deliveryCompany,
        String trackingCode
) {}
