package com.example.food_mart.modules.shop.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.common.utils.MoneyCalculation;
import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemInCart;
import com.example.food_mart.modules.shop.domain.repository.ItemInCartRepository;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.shop.presentataion.dto.request.ItemInCartCreateDTO;
import com.example.food_mart.modules.warehouse.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
/*
    Cart라는 엔티티는 없음, 도메인 객체임
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final ItemInCartRepository itemInCartRepository;
    private final ItemRepository itemRepository;
    private final StockService stockService;

    /*
        도메인 객체 세팅
        장바구니 아이템(ItemInCart)들을 기반으로 도메인 객체(Cart)세팅
     */
    public void settingCart(Long userId, Cart cart) {
        // 장바구니에 있는 아이템들 도메인 객체에 담기
        List<ItemInCart> itemsInCart = selectItemsInCart(userId);
        cart.setItemsInCart(itemsInCart);

        // 필요 로직 진행
        cart.calculateTotalPrice();
        cart.countItems();
    }

    /*
        장바구니에 아이템 저장
        1. Item조회     2. 조회한 정보로 저장
     */
    public Long saveItemInCart(ItemInCartCreateDTO itemInCartCreateDTO, Long userId) {
//        UserUtils.checkLogin(userId);

        // Item조회
        Long itemId = itemInCartCreateDTO.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Expected4xxException(ErrorCode.ITEM_NOT_FOUND));

        int itemCount = itemInCartCreateDTO.getCount();
        long totalPrice = MoneyCalculation.priceCount(item.getPrice(), itemCount);

        // ItemInCart저장
        ItemInCart itemInCart = new ItemInCart(userId, itemId, item.getName(), itemCount, totalPrice);
        ItemInCart savedItemInCart = itemInCartRepository.save(itemInCart);
        return savedItemInCart.getId();
    }

    /*
        장바구니에 담긴 item과 Stock개수 비교
         1. 장바구니에 있는 아이템 가져오기   2. 각 아이템별 Stock개수 파악
     */
    public String buyableCount(Cart cart) {
        List<ItemInCart> itemsInCart = cart.getItemsInCart();

        for (ItemInCart itemInCart : itemsInCart) {
            Item item = itemRepository.findById(itemInCart.getItemId()).orElse(null);
            if (item == null) {
                return itemInCart.getName() + "는 존재하지 않습니다.";
            }

            // 각 아이템별 Stock개수 파악
            Long totalStock = stockService.countStockForItem(item);
            if (itemInCart.getCount() > totalStock) {
                return item.getName() + "의 재고가 부족합니다.";
            }
        }
        return "ok";
    }

    /*
        주문 완료후, 장바구니 비우기
        userId에 해당하는 것들 지움
     */
    public void emptyCartAfterOrder(Long userId, Cart cart) {
        cart = null;
        try {
            itemInCartRepository.deleteByUserId(userId);
        } catch (Exception ignored) {}
    }

    /*
        장바구니에 있는 아이템들 가져오기
     */
    public List<ItemInCart> selectItemsInCart(Long userId) {
        List<ItemInCart> itemsInCart = itemInCartRepository.findAllByUserId(userId);

        // findAllBy 결과값 확인해야함
        if (itemsInCart.isEmpty()) {
            throw new Expected4xxException(ErrorCode.ZERO_CART);
        }
        return itemsInCart;
    }
}
