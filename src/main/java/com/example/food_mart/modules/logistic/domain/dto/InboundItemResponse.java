package com.example.food_mart.modules.logistic.domain.dto;

import com.example.food_mart.modules.logistic.domain.entity.InboundItem;
import com.example.food_mart.modules.logistic.domain.entity.InboundStackingStatus;

import java.time.LocalDateTime;

public record InboundItemResponse(
        Long id,
        Long itemId,
        String itemName,
        Long count,
        InboundStackingStatus inboundStackingStatus,
        LocalDateTime createdAt
) {
    public static InboundItemResponse from(InboundItem ii) {
        return new InboundItemResponse(
                ii.getId(),
                ii.getItem().getId(),
                ii.getItem().getName(),
                ii.getCount(),
                ii.getInboundStackingStatus(),
                ii.getCreatedAt()
        );
    }
}
