package com.example.food_mart.modules.logistic;

import com.example.food_mart.common.argumentResolver.StaffInfo;
import com.example.food_mart.modules.logistic.application.OutboundService;
import com.example.food_mart.modules.logistic.domain.entity.DeliveryCompany;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/outbound")
@RequiredArgsConstructor
public class OutboundApiController {

    private final OutboundService outboundService;

    /*
        출고 등록
     */
    record OutboundCreateDTO(Long orderId, String address, DeliveryCompany deliveryCompany, String trackingCode) {}

    public Long outbound(@RequestBody OutboundCreateDTO dto, StaffInfo staffInfo) {
        Long outboundId = outboundService.registerOutbound(dto.orderId(), dto.address(), dto.deliveryCompany(), dto.trackingCode(), staffInfo.getStaffId());
        return outboundId;
    }
}
