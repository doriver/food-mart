package com.example.food_mart.modules.order.domain.entity;

import com.example.food_mart.modules.shop.domain.entity.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_order", columnList = "order_id"),
        @Index(name = "idx_item", columnList = "item_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Item item;

    public Long getOrderId() { return order.getId(); }
    public Long getItemId() { return item.getId(); }

    private String name;

    private int count;

    private long totalPrice;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public OrderItem(Order order, Item item, String name, int count, long totalPrice, LocalDateTime createdAt) {
        this.order = order;
        this.item = item;
        this.name = name;
        this.count = count;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }
}
