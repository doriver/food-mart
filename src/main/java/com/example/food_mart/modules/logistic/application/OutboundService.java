package com.example.food_mart.modules.logistic.application;

import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.logistic.domain.entity.Delivery;
import com.example.food_mart.modules.logistic.domain.entity.DeliveryCompany;
import com.example.food_mart.modules.logistic.domain.entity.Outbound;
import com.example.food_mart.modules.logistic.domain.entity.OutboundStatus;
import com.example.food_mart.modules.logistic.domain.repository.DeliveryRepository;
import com.example.food_mart.modules.logistic.domain.repository.OutboundRepository;
import com.example.food_mart.modules.warehouse.domain.entity.Picking;
import com.example.food_mart.modules.warehouse.domain.entity.PickingStatus;
import com.example.food_mart.modules.warehouse.domain.repository.PickingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundService {

    private final DeliveryRepository deliveryRepository;
    private final OutboundRepository outboundRepository;
    private final PickingRepository pickingRepository;

    @Transactional
    public Long registerOutbound(Long orderId, String address, DeliveryCompany deliveryCompany, String trackingCode, Long staffId) {

        // 피킹 상태 확인
        List<Picking> pickingList = pickingRepository.findAllByOrderId(orderId);
        for (Picking picking : pickingList) {
            if (picking.getPickingStatus() != PickingStatus.COMPLETED) {
                throw new Expected4xxException("아직 재고 피킹이 안됐습니다");
            }
        }

        Delivery delivery = deliveryRepository.save(new Delivery(orderId, address, deliveryCompany, trackingCode));
        Outbound outbound = outboundRepository.save(new Outbound(staffId, delivery.getId(), OutboundStatus.READY));
        return outbound.getId();
    }
}
