package com.example.food_mart.modules.shop.domain;

import com.example.food_mart.modules.shop.domain.entity.ItemInCart;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Cart {
    private long totalPrice;
    @Setter
    private List<ItemInCart> itemsInCart; // OrderItem 저장할때 사용됨
    private Map<Long, Integer> itemAndCountMap; // 아이템id와 그 갯수


    /*
        장바구니에 있는 item들 총 가격 구하기
     */
    public void calculateTotalPrice() {
        long totalPrice = 0;
        for (ItemInCart itemInCart: itemsInCart) {
            totalPrice += itemInCart.getTotalPrice();
        }
        this.totalPrice = totalPrice;
    }

    /*
        아이템id와 그 갯수
     */
    public void countItems() {
        Map<Long, Integer> itemAndCountMap = new HashMap<>();
        for (ItemInCart itemInCart : itemsInCart) {
            if (itemAndCountMap.containsKey(itemInCart.getItemId())) {
                itemAndCountMap.put(
                        itemInCart.getItemId()
                        , itemAndCountMap.get(itemInCart.getItemId()) + itemInCart.getCount());
            } else {
                itemAndCountMap.put(itemInCart.getItemId(),itemInCart.getCount());
            }
        }
        this.itemAndCountMap = itemAndCountMap;
    }

}
