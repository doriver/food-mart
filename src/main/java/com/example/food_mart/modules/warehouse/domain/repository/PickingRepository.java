package com.example.food_mart.modules.warehouse.domain.repository;

import com.example.food_mart.modules.warehouse.domain.entity.Picking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickingRepository extends JpaRepository<Picking, Long> {
}
