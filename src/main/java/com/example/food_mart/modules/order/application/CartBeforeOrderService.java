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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartBeforeOrderService {

    private final WalletReadService walletReadService;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
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
         1. 장바구니에 있는 아이템 가져오기   2. 거기의 개수와 Stock개수 비교하기
     */
    public String buyableCount(Cart cart) {
        List<ItemInCart> itemsInCart = cart.getItemsInCart();

        for (ItemInCart itemInCart : itemsInCart) {
            Item item = itemRepository.findById(itemInCart.getItemId()).orElse(null);
            if (item == null) {
                return itemInCart.getName() + "는 존재하지 않습니다.";
            }

            ItemStorage itemStorage = item.getItemStorage();
            List<Warehouse> warehouseList = warehouseRepository.findByWarehousePurposeIn(
                    List.of(itemStorage.toWarehousePurpose(), WarehousePurpose.IN));
            List<Stock> stockList = stockRepository.findByWarehouseIdInAndItemId(
                    List.of(warehouseList.get(0).getId(), warehouseList.get(1).getId()), item.getId()
            );

            Long totalStock = stockList.stream()
                    .mapToLong(Stock::getCount)
                    .sum();

            if (itemInCart.getCount() > totalStock) {
                return item.getName() + "의 재고가 부족합니다.";
            }
        }
        return "ok";
    }
}
