package com.example.food_mart.modules.warehouse.domain.mapper;

public record ItemStockSummaryRow(
        Long itemId,
        String itemName,
        Long totalStock,
        String warehouses
) {}
