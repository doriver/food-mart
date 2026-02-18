package com.example.food_mart.modules.warehouse.domain.repository;

import com.example.food_mart.modules.warehouse.domain.entity.Picking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PickingRepository extends JpaRepository<Picking, Long> {

    List<Picking> findAllByOrderId(Long orderId);
}
