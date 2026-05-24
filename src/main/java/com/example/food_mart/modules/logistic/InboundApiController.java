package com.example.food_mart.modules.logistic;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.StaffInfo;
import com.example.food_mart.modules.logistic.application.InboundService;
import com.example.food_mart.modules.staff.domain.StaffRole;
import com.example.food_mart.modules.warehouse.application.StackingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
public class InboundApiController {

    private final InboundService inboundService;
    private final StackingService stackingService;
    
    @Operation(summary = "특정 입고의 아이템 목록 조회")
    @GetMapping("/{inboundId}/items")
    public ApiResponse getInboundItems(@PathVariable Long inboundId) {
        return ApiResponse.success(inboundService.getInboundItems(inboundId));
    }

    /*
        입고 등록
     */
    record InboundCreateDTO(String supplier, Map<Long,Long> itemAndCount) {}

    @Operation(summary = "입고 등록")
    @PostMapping
    public ApiResponse inbound(@RequestBody InboundCreateDTO inboundCreateDTO, StaffInfo staffInfo) {
        Long inboundId = inboundService.registerInbound(staffInfo.getStaffId(), inboundCreateDTO.supplier(), inboundCreateDTO.itemAndCount());
        return ApiResponse.success(inboundId);
    }

    /*
        특정 입고 아이템 창고에 적재완료
     */
    record StackingDTO(Long inboundItemId, Map<Long,Long> stockAndCount) {}

    @Operation(summary = "특정 입고 아이템 창고에 적재완료")
    @PostMapping("/stacking")
    public ApiResponse completeStacking(@RequestBody StackingDTO dto, StaffInfo staffInfo) {
        stackingService.doCompleteStacking(dto.inboundItemId(), staffInfo.getStaffId(), dto.stockAndCount());
        return ApiResponse.success();
    }
}
