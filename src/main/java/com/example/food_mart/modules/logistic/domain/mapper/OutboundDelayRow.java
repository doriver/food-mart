package com.example.food_mart.modules.logistic.domain.mapper;

public record OutboundDelayRow(
        Long orderId,
        String orderedAt,
        String customer,
        String deliveryCompany,
        String outboundStatus,
        Long hoursElapsed
) {}
