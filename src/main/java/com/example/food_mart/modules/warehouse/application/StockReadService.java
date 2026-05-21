package com.example.food_mart.modules.warehouse.application;

import com.example.food_mart.modules.warehouse.domain.mapper.ItemStockSummaryMapper;
import com.example.food_mart.modules.warehouse.domain.mapper.ItemStockSummaryRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockReadService {

    private final ItemStockSummaryMapper itemStockSummaryMapper;

    public List<ItemStockSummaryRow> getItemStockSummary() {
        return itemStockSummaryMapper.findAllItemStockSummary();
    }
}
