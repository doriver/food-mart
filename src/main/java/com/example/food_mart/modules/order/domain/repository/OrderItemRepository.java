package com.example.food_mart.modules.order.domain.repository;

import com.example.food_mart.modules.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderId(Long orderId);
}
