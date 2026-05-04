package com.example.food_mart.modules.shop.presentataion.dto.response;

import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemStatus;
import com.example.food_mart.modules.shop.domain.entity.ItemStorage;

import java.util.Map;

public record ItemResponse(
        Long id,
        String name,
        int price,
        ItemStorage itemStorage,
        Map<String, Object> attribute,
        Long categoryId,
        String description,
        String imagePath,
        ItemStatus status
) {
    public static ItemResponse from(Item item) {
        String imagePath = item.getImagePath() != null
                ? "/uploads/items/" + item.getImagePath()
                : null;
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getItemStorage(),
                item.getAttribute(),
                item.getCategoryId(),
                item.getDescription(),
                imagePath,
                item.getStatus()
        );
    }
}
