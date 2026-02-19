package com.example.food_mart.modules.logistic;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.StaffInfo;
import com.example.food_mart.modules.logistic.application.InboundService;
import com.example.food_mart.modules.staff.domain.StaffRole;
import com.example.food_mart.modules.warehouse.application.StackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
public class InboundApiController {

    private final InboundService inboundService;
    private final StackingService stackingService;
    /*
        입고 등록
     */
    record InboundCreateDTO(String supplier, Map<Long,Long> itemAndCount) {}

    @PostMapping
    public ApiResponse inbound(@RequestBody InboundCreateDTO inboundCreateDTO, StaffInfo staffInfo) {
        Long inboundId = inboundService.registerInbound(staffInfo.getStaffId(), inboundCreateDTO.supplier(), inboundCreateDTO.itemAndCount());
        return ApiResponse.success(inboundId);
    }

    /*
        특정 입고 아이템 창고에 적재완료
     */
    record StackingDTO(Long inboundItemId, Map<Long,Long> stockAndCount) {}

    @PostMapping("/stacking")
    public ApiResponse completeStacking(@RequestBody StackingDTO dto, StaffInfo staffInfo) {
        stackingService.doCompleteStacking(dto.inboundItemId(), staffInfo.getStaffId(), dto.stockAndCount());
        return ApiResponse.success();
    }
}
