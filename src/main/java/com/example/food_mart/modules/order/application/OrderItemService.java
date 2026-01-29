package com.example.food_mart.modules.order.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.order.domain.entity.OrderItem;
import com.example.food_mart.modules.order.domain.repository.OrderItemRepository;
import com.example.food_mart.modules.shop.domain.entity.ItemInCart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    /*
        주문된 아이템 등록
     */
    public void saveOrderItem(Long savedOrderId, List<ItemInCart> itemsInCart) {

        List<OrderItem> orderItems = cartIntoOrder(savedOrderId, itemsInCart);

        try {
            orderItemRepository.saveAll(orderItems);
        } catch (Exception e) {
            throw new Expected4xxException(ErrorCode.FAIL_ORDER_ITEM);
        }
    }


    /*
        카트에 담긴 Item들 OrderItem으로
    */
    public List<OrderItem> cartIntoOrder(Long orderId, List<ItemInCart> itemsInCart) {

        List<OrderItem> orderItemList = new ArrayList<>();
        for (ItemInCart itemInCart : itemsInCart) {
            OrderItem orderItem = new OrderItem(orderId, itemInCart.getItemId(), itemInCart.getName(), itemInCart.getCount(), itemInCart.getTotalPrice(), LocalDateTime.now());
            orderItemList.add(orderItem);
        }
        return orderItemList;
    }

}
