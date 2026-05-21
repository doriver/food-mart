package com.example.food_mart.modules.shop.domain.entity;

import com.example.food_mart.modules.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
    cart는 redis로 할수도 있음
 */
@Entity
@Table(indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_item", columnList = "item_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemInCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Item item;

    public Long getUserId() { return user.getId(); }
    public Long getItemId() { return item.getId(); }

    private String name;

    private int count;

    private long totalPrice;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ItemInCart(User user, Item item, String name, int count, long totalPrice) {
        this.user = user;
        this.item = item;
        this.name = name;
        this.count = count;
        this.totalPrice = totalPrice;
    }
}
