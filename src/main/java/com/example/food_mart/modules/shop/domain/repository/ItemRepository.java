package com.example.food_mart.modules.shop.domain.repository;

import com.example.food_mart.modules.shop.domain.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
