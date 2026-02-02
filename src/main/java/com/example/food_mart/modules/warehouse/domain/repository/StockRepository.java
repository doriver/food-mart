package com.example.food_mart.modules.warehouse.domain.repository;

import com.example.food_mart.modules.warehouse.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    // select * from table where a in (...) and b = ?
    List<Stock> findByWarehouseIdInAndItemId(List<Long> warehouseIds, Long itemId);

    Optional<Stock> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);
}
