package com.example.food_mart.modules.shop.domain.repository;

import com.example.food_mart.modules.shop.domain.entity.ItemSalesCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemSalesCountRepository extends JpaRepository<ItemSalesCount, Long> {
    Page<ItemSalesCount> findByCategoryId(Long categoryId, Pageable pageable);
    void deleteByItemIdIn(List<Long> itemIds);
}
