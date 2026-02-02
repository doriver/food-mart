package com.example.food_mart.modules.shop.domain.entity;

import com.example.food_mart.common.exception.Expected4xxException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(indexes = {
        @Index(name = "idx_category", columnList = "categoryId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ItemStorage itemStorage;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> attribute;

    private Long categoryId;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false
    )
    private LocalDateTime updatedAt;

    public Item(String name, int price, ItemStorage itemStorage, Map<String, Object> attribute, Long categoryId) {
        this.name = name;
        this.price = price;
        this.itemStorage = itemStorage;
        this.attribute = attribute;
        this.categoryId = categoryId;
    }
}
