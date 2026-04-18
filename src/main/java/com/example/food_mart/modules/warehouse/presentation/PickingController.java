package com.example.food_mart.modules.warehouse.presentation;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.StaffInfo;
import com.example.food_mart.modules.warehouse.application.PickingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PickingController {

    private final PickingService pickingService;

    @Operation(summary = "피킹 완료 처리")
    @PatchMapping("/pickings/{pickingId}/complete")
    public ApiResponse<Long> completePicking(@PathVariable Long pickingId,
                                             StaffInfo staffInfo) {
        Long updatedId = pickingService.completePicking(pickingId, staffInfo.getStaffId());
        return ApiResponse.success(updatedId);
    }
}
