package com.example.food_mart.modules.order.presentation.dto.response;

import lombok.Getter;

@Getter
public class OrderedItemDTO {

    private String name;
    private int count;
    private long totalPrice;

    public OrderedItemDTO(String name, int count, long totalPrice) {
        this.name = name;
        this.count = count;
        this.totalPrice = totalPrice;
    }
}
