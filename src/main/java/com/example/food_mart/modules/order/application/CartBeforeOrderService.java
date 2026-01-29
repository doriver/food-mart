package com.example.food_mart.modules.order.application;

import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemInCart;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.user.application.WalletReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartBeforeOrderService {

    private final WalletReadService walletReadService;
    private final ItemRepository itemRepository;

    /*
        장바구니에 있는 Item들 총 가격, 구매자의 있는돈 비교하기
     */
    public boolean buyablePrice(Long userId, long totalPrice) {

        long money = walletReadService.selectMoney(userId);

        return money >= totalPrice;
    }

    /*
        장바구니에 담긴 item과 실제 item 개수 비교
         1. 장바구니에 있는 아이템 가져오기   2. 거기의 개수와 item개수 비교하기
     */
    public String buyableCount(Cart cart) {
        List<ItemInCart> itemsInCart = cart.getItemsInCart();

        for (ItemInCart itemInCart : itemsInCart) {
            Item item = itemRepository.findById(itemInCart.getItemId()).orElse(null);
            if (item == null) {
                return itemInCart.getName() + "는 존재하지 않습니다.";
            }

            if (itemInCart.getCount() > item.getCount()) {
                return item.getName() + "의 재고가 부족합니다.";
            }
        }
        return "ok";
    }
}
