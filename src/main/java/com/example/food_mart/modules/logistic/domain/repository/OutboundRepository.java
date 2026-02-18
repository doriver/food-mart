package com.example.food_mart.modules.logistic.domain.repository;

import com.example.food_mart.modules.logistic.domain.entity.Outbound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboundRepository extends JpaRepository<Outbound, Long> {
}
