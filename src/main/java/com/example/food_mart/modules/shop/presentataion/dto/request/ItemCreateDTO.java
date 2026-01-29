package com.example.food_mart.modules.shop.presentataion.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCreateDTO {
    private String name;
    private int price;
    private int count;
}
