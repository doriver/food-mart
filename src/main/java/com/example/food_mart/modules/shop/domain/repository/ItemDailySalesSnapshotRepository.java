package com.example.food_mart.modules.shop.domain.repository;

import com.example.food_mart.modules.shop.domain.entity.ItemDailySalesSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ItemDailySalesSnapshotRepository extends JpaRepository<ItemDailySalesSnapshot, Long> {

    List<ItemDailySalesSnapshot> findAllBySalesDate(LocalDate salesDate);
}
