package com.example.food_mart.modules.order.presentation.dto.response;

import com.example.food_mart.modules.order.domain.entity.OrderStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter // 이걸로 안되면 , @NoArgsConstructor(access = AccessLevel.PROTECTED) 넣기
public class OrderReadDTO {
    private Long id;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public OrderReadDTO(Long id, OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }
}
