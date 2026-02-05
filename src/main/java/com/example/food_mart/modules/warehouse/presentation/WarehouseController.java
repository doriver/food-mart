package com.example.food_mart.modules.warehouse.presentation;


import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.modules.warehouse.application.WarehouseService;
import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    /*
        창고 등록
        @param: 이름, 위치, 창고목적
     */

    record WarehouseCreateDTO(String name, String location, WarehousePurpose warehousePurpose) {}
    @PostMapping("/warehouses")
    public ApiResponse<Long> registerWarehouse(@RequestBody WarehouseCreateDTO dto, UserInfo userInfo) {
        Long registeredId = warehouseService.registerWarehouse(dto.name(), dto.location(), dto.warehousePurpose());
        return ApiResponse.success(registeredId);
    }
}
