package com.example.food_mart.modules.logistic.domain.repository;

import com.example.food_mart.modules.logistic.domain.entity.Outbound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OutboundRepository extends JpaRepository<Outbound, Long> {
    Optional<Outbound> findByDeliveryId(Long deliveryId);
}
