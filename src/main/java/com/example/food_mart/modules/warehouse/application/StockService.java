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

import java.util.*;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;

    // 재고 등록
    public Long registerStock(Long count, WarehousePurpose locationType, Long itemId, Long warehouseId) {
        Stock saved = stockRepository.save(
                new Stock(count, locationType, itemId, warehouseId));
        return saved.getId();
    }

    // 특정 item의 stock들 파악
    public List<Stock> getItemStockList(Item item) {
        List<Stock> stockList = stockRepository.findAllByItemId(item.getId());
        if (stockList.isEmpty()) {
            throw new Expected4xxException(item.getName() + "의 재고가 없습니다.");
        }

        Set<WarehousePurpose> targets = EnumSet.of(
                item.getItemStorage().toWarehousePurpose()); // 나중에 WarehousePurpose.IN 도 여기에 넣는걸로, 이거 넣으면 복잡해짐
        List<Stock> filteredstockList = stockList.stream()
                .filter(stock -> targets.contains(stock.getLocationType()))
                .toList();
        return filteredstockList;
    }

    // 특정 item의 재고 개수 파악
    public Long countStockForItem(Item item) {
        List<Stock> stockList = getItemStockList(item);

        Long totalStock = stockList.stream()
                .mapToLong(Stock::getCount)
                .sum();
        return totalStock;
    }


    // 창고에 있는 재고, 배송대기 상태로
    @Transactional
    public void stockToOutPrepare(Map<Long, Integer> itemAndCount) {
        List<Stock> changedStockList = new ArrayList<>();

        for (Long itemId : itemAndCount.keySet()) {
        // 1. 각 아이템 재고찾기
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new Expected4xxException(ErrorCode.ITEM_NOT_FOUND));
            List<Stock> stockList = getItemStockList(item);

        // 2. Stock 배송대기 상태로( 재고 감소 + 오더 피킹 생성 )
            Integer itemCount = itemAndCount.get(itemId);
            long remainOrderItem = itemCount;
            // 창고가 많아지는 경우는, 어디서부터 뺄지 결정하는것도 중요해질수 있음
            for (Stock stock :stockList) {
                remainOrderItem = stock.minusCount(remainOrderItem);
                changedStockList.add(stock);
                if (remainOrderItem == 0) {
                    break;
                }
            }

            // 오더 피킹 생성
        }
        try {
            stockRepository.saveAll(changedStockList);
        } catch (Exception e) {
            throw new Expected4xxException(ErrorCode.FAIL_STOCK_COUNT_DOWN);
        }
    }
}
