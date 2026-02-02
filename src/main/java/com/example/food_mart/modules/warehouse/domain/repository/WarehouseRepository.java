package com.example.food_mart.modules.warehouse.domain.repository;

import com.example.food_mart.modules.warehouse.domain.entity.Warehouse;
import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Warehouse findByWarehousePurpose(WarehousePurpose warehousePurpose);
    List<Warehouse> findByWarehousePurposeIn(List<WarehousePurpose> purposes);
}
