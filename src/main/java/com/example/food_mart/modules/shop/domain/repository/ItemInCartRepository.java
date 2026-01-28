package com.example.food_mart.modules.shop.domain.repository;

import com.example.food_mart.modules.shop.domain.entity.ItemInCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemInCartRepository extends JpaRepository<ItemInCart, Long> {

    List<ItemInCart> findAllByUserId(Long userId);

    @Transactional
    void deleteByUserId(Long userId);
}
