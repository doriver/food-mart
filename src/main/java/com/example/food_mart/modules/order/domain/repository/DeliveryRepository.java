package com.example.food_mart.modules.order.domain.repository;

import com.example.food_mart.modules.order.domain.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
