package com.example.food_mart.modules.warehouse.domain.repository;

import com.example.food_mart.modules.warehouse.domain.entity.InOutHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InOutHistoryRepository extends JpaRepository<InOutHistory, Long> {
}
