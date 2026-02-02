package com.example.food_mart.modules.warehouse.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemStorage;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.warehouse.domain.entity.Stock;
import com.example.food_mart.modules.warehouse.domain.entity.Warehouse;
import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
import com.example.food_mart.modules.warehouse.domain.repository.StockRepository;
import com.example.food_mart.modules.warehouse.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;

    // 창고에 있는 재고, 배송대기 상태로(out창고로 이동)
    @Transactional
    public void stockToOutPrepare(Map<Long, Integer> itemAndCount) {
        List<Stock> moveStock = new ArrayList<>();

        for (Long itemId : itemAndCount.keySet()) {
        // 1. 각 아이템 재고찾기
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new Expected4xxException(ErrorCode.ITEM_NOT_FOUND));
            ItemStorage itemStorage = item.getItemStorage();
            // 아이템이 있는 창고찾기
            Warehouse warehouse = warehouseRepository.findByWarehousePurpose(itemStorage.toWarehousePurpose());
            // 아이템 재고찾기
            Stock stock = stockRepository.findByWarehouseIdAndItemId(warehouse.getId(), item.getId())
                    .orElseThrow(() -> new Expected4xxException(ErrorCode.STOCK_NOT_FOUND));

        // 2. Stock 배송대기 상태로(out창고로 이동)
            Warehouse outWarehouse = warehouseRepository.findByWarehousePurpose(WarehousePurpose.OUT);
            Stock outStock = stockRepository.findByWarehouseIdAndItemId(outWarehouse.getId(), item.getId())
                    .orElse(new Stock(0L, item.getId(), outWarehouse.getId()));

            Integer itemCount = itemAndCount.get(itemId);
            stock.minusCount(itemCount);
            outStock.plusCount(itemCount);
            moveStock.addAll(List.of(stock,outStock));
        }
        try {
            stockRepository.saveAll(moveStock);
        } catch (Exception e) {
            throw new Expected4xxException(ErrorCode.FAIL_STOCK_COUNT_DOWN);
        }
    }
}
