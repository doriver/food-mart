package com.example.food_mart.modules.order.domain.repository;

import com.example.food_mart.modules.order.domain.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    List<OrderHistory> findAllByOrderId(Long orderId);
}
