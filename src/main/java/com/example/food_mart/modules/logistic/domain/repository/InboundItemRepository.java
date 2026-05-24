package com.example.food_mart.modules.logistic.domain.repository;

import com.example.food_mart.modules.logistic.domain.entity.InboundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InboundItemRepository extends JpaRepository<InboundItem, Long> {

    @Query("SELECT ii FROM InboundItem ii JOIN FETCH ii.item WHERE ii.inbound.id = :inboundId")
    List<InboundItem> findAllByInboundIdWithItem(@Param("inboundId") Long inboundId);
}
