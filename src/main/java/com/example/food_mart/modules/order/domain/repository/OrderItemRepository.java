package com.example.food_mart.modules.order.domain.repository;

import com.example.food_mart.modules.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderId(Long orderId);

    @Query("SELECT oi.itemId, SUM(oi.count) FROM OrderItem oi WHERE oi.createdAt >= :since GROUP BY oi.itemId")
    List<Object[]> sumWeeklyCountByItemId(@Param("since") LocalDateTime since);
}
