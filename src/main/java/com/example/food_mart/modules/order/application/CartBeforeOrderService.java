package com.example.food_mart.modules.order.application;

import com.example.food_mart.modules.shop.domain.Cart;
import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemInCart;
import com.example.food_mart.modules.shop.domain.entity.ItemStorage;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.user.application.WalletReadService;
import com.example.food_mart.modules.warehouse.domain.entity.Stock;
import com.example.food_mart.modules.warehouse.domain.entity.Warehouse;
import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
import com.example.food_mart.modules.warehouse.domain.repository.StockRepository;
import com.example.food_mart.modules.warehouse.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartBeforeOrderService {

    private final WalletReadService walletReadService;
    private final ItemRepository itemRepository;
    private final StockRepository stockRepository;

    /*
        장바구니에 있는 Item들 총 가격, 구매자의 있는돈 비교하기
     */
    public boolean buyablePrice(Long userId, long totalPrice) {

        long money = walletReadService.selectMoney(userId);

        return money >= totalPrice;
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

            List<Stock> stockList = stockRepository.findAllByItemId(item.getId());

            Set<WarehousePurpose> targets = EnumSet.of(
                    item.getItemStorage().toWarehousePurpose(), WarehousePurpose.IN);
            List<Stock> filteredstockList = stockList.stream()
                    .filter(stock -> targets.contains(stock.getLocationType()))
                    .toList();

            Long totalStock = filteredstockList.stream()
                    .mapToLong(Stock::getCount)
                    .sum();

            if (itemInCart.getCount() > totalStock) {
                return item.getName() + "의 재고가 부족합니다.";
            }
        }
        return "ok";
    }
}
