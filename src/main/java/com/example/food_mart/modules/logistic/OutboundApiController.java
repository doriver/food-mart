package com.example.food_mart.modules.logistic;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.StaffInfo;
import com.example.food_mart.modules.logistic.application.OutboundService;
import com.example.food_mart.modules.logistic.domain.entity.DeliveryCompany;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outbound")
@RequiredArgsConstructor
public class OutboundApiController {

    private final OutboundService outboundService;

    /*
        출고 등록
     */
    record OutboundCreateDTO(Long orderId, String address, DeliveryCompany deliveryCompany, String trackingCode) {}

    @Operation(summary = "출고 등록")
    @PostMapping
    public Long outbound(@RequestBody OutboundCreateDTO dto, StaffInfo staffInfo) {
        Long outboundId = outboundService.registerOutbound(dto.orderId(), dto.address(), dto.deliveryCompany(), dto.trackingCode(), staffInfo.getStaffId());
        return outboundId;
    }

    @Operation(summary = "출고 완료 처리") // Outbound의 상태를 COMPLETED로 업데이트, staffId도 같이 업데이트
    @PatchMapping("/{outboundId}/complete")
    public ApiResponse<Long> completeOutbound(@PathVariable Long outboundId, StaffInfo staffInfo) {
        Long updatedId = outboundService.completeOutbound(outboundId, staffInfo.getStaffId());
        return ApiResponse.success(updatedId);
    }
}
