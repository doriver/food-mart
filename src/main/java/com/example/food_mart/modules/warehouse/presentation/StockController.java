package com.example.food_mart.modules.warehouse.presentation;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.modules.warehouse.application.StockReadService;
import com.example.food_mart.modules.warehouse.application.StockService;
import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
import com.example.food_mart.modules.warehouse.domain.mapper.ItemStockSummaryRow;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockReadService stockReadService;
    /*
        재고 등록
        @param
        나중에 입고쪽이랑 연관해서 변경할수도
     */
    record StockCreateDTO(Long count, WarehousePurpose locationType, Long itemId, Long warehouseId) {}
    @Operation(summary = "재고등록")
    @PostMapping("/stocks")
    public ApiResponse<Long> registerStock(@RequestBody StockCreateDTO dto, UserInfo userInfo) {
        Long registeredId = stockService.registerStock(dto.count(), dto.locationType(), dto.itemId(), dto.warehouseId());
        return ApiResponse.success(registeredId);
    }

    @Operation(summary = "상품별 재고 총합 (창고 통합)")
    @GetMapping("/stocks/summary")
    public ApiResponse<List<ItemStockSummaryRow>> getItemStockSummary() {
        return ApiResponse.success(stockReadService.getItemStockSummary());
    }
}
