package com.example.food_mart.modules.logistic.domain.repository;

import com.example.food_mart.modules.logistic.domain.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
}
