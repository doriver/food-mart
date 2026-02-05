package com.example.food_mart.modules.warehouse.presentation;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.modules.warehouse.application.StockService;
import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    /*
        재고 등록
        @param
        나중에 입고쪽이랑 연관해서 변경할수도
     */
    record StockCreateDTO(Long count, WarehousePurpose locationType, Long itemId, Long warehouseId) {}
    @PostMapping("/stocks")
    public ApiResponse<Long> registerStock(@RequestBody StockCreateDTO dto, UserInfo userInfo) {
        Long registeredId = stockService.registerStock(dto.count(), dto.locationType(), dto.itemId(), dto.warehouseId());
        return ApiResponse.success(registeredId);
    }
}
