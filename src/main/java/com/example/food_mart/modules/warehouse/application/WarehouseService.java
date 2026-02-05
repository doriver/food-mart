package com.example.food_mart.modules.warehouse.application;

import com.example.food_mart.modules.warehouse.domain.entity.Warehouse;
import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
import com.example.food_mart.modules.warehouse.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    /*
        창고 등록
     */
    public Long registerWarehouse(String name, String location, WarehousePurpose warehousePurpose) {
        Warehouse warehouse = new Warehouse(name, location, warehousePurpose);
        Warehouse saved = warehouseRepository.save(warehouse);
        return saved.getId();
    }
}
