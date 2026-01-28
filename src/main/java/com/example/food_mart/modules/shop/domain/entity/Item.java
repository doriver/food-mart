package com.example.food_mart.modules.shop.domain.entity;

import com.example.food_mart.common.exception.Expected4xxException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
    userId, count 추가해서 유저별로 아이템 등록하는거 고려
    아니면, userId 안넣고, 제고관리쪽 이랑 엮을수도 있나?
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;

    private int count;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public Item(String name, int price, int count, LocalDateTime createdAt) {
        this.name = name;
        this.price = price;
        this.count = count;
        this.createdAt = createdAt;
    }

    public void minusCount(int mc) {
        if (mc <= count) {
            this.count = count - mc;
        } else {
            throw new Expected4xxException(name + "의 개수가 부족합니다.");
        }
    }
}
