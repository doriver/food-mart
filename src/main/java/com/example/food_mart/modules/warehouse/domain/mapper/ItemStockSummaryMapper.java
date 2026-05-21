package com.example.food_mart.modules.warehouse.domain.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemStockSummaryMapper {

    List<ItemStockSummaryRow> findAllItemStockSummary();
}
