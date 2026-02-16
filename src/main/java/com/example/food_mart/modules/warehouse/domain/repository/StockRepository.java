package com.example.food_mart.modules.warehouse.domain.repository;

import com.example.food_mart.modules.warehouse.domain.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    // select * from table where a in (...) and b = ?
    List<Stock> findByWarehouseIdInAndItemId(List<Long> warehouseIds, Long itemId);

    Optional<Stock> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);

    List<Stock> findAllByItemId(Long itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.id = :id")
    Optional<Stock> findByIdWithPessimisticLock(@Param("id") Long id);
}
