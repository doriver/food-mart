package com.example.food_mart.modules.shop.presentataion.dto.request;

import com.example.food_mart.modules.shop.domain.entity.ItemStorage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCreateDTO {
    private String name;
    private int price;
    private ItemStorage itemStorage;
    private Map<String, Object> attribute;
    private Long categoryId;
}
