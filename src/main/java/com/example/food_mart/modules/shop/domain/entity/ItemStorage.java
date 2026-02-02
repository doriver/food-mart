package com.example.food_mart.modules.shop.domain.entity;

import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
/*
    enum 잘모르고 일단 그냥 써봄, 수정해야할수도 
 */
public enum ItemStorage {
    ROOMTEMPERATURE(WarehousePurpose.ROOMTEMPERATURE),
    COLD(WarehousePurpose.COLD),
    FREEZER(WarehousePurpose.FREEZER);

    private final WarehousePurpose purpose;

    ItemStorage(WarehousePurpose purpose) {
        this.purpose = purpose;
    }

    public WarehousePurpose toWarehousePurpose() {
        return this.purpose;
    }
}
