package com.example.food_mart.modules.logistic.domain.repository;

import com.example.food_mart.modules.logistic.domain.entity.InboundItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundItemRepository extends JpaRepository<InboundItem, Long> {
}
