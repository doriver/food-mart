package com.example.food_mart.modules.shop.domain.repository;

import com.example.food_mart.modules.shop.domain.entity.ShopLedgerHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopLedgerHistoryRepository extends JpaRepository<ShopLedgerHistory, Long> {
}
