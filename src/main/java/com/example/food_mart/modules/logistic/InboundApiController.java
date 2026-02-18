package com.example.food_mart.modules.logistic;

import com.example.food_mart.common.argumentResolver.StaffInfo;
import com.example.food_mart.modules.logistic.application.InboundService;
import com.example.food_mart.modules.staff.domain.StaffRole;
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
    /*
        입고
        todo: 입고된 물건들 창고에 뿌려주는 기능도 필요
     */
    record InboundCreateDTO(String supplier, Map<Long,Long> itemAndCount) {}

    @PostMapping
    public Long inbound(@RequestBody InboundCreateDTO inboundCreateDTO, StaffInfo staffInfo) {
        Long inboundId = inboundService.registerInbound(staffInfo.getStaffId(), inboundCreateDTO.supplier(), inboundCreateDTO.itemAndCount());
        return inboundId;
    }
}
