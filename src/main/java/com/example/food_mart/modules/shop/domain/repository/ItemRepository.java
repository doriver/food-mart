package com.example.food_mart.modules.shop.domain.repository;

import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByStatusIn(List<ItemStatus> statuses, Pageable pageable);
    Page<Item> findByCategoryIdAndStatusIn(Long categoryId, List<ItemStatus> statuses, Pageable pageable);
}
