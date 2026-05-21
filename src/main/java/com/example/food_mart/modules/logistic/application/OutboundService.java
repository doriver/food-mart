package com.example.food_mart.modules.logistic.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.logistic.domain.entity.Delivery;
import com.example.food_mart.modules.logistic.domain.entity.DeliveryCompany;
import com.example.food_mart.modules.logistic.domain.entity.Outbound;
import com.example.food_mart.modules.logistic.domain.entity.OutboundStatus;
import com.example.food_mart.modules.logistic.domain.repository.DeliveryRepository;
import com.example.food_mart.modules.logistic.domain.repository.OutboundRepository;
import com.example.food_mart.modules.staff.domain.Staff;
import com.example.food_mart.modules.staff.domain.StaffRepository;
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
    private final StaffRepository staffRepository;

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
        Staff staff = staffRepository.getReferenceById(staffId);
        Outbound outbound = outboundRepository.save(new Outbound(staff, delivery.getId(), OutboundStatus.READY));
        return outbound.getId();
    }

    @Transactional
    public Long completeOutbound(Long outboundId, Long staffId) {
        Outbound outbound = outboundRepository.findById(outboundId)
                .orElseThrow(() -> new Expected4xxException(ErrorCode.NOT_FOUND_OUTBOUND));

        if (outbound.getOutboundStatus() != OutboundStatus.READY) {
            throw new Expected4xxException(ErrorCode.NOT_READY_OUTBOUND);
        }

        outbound.completeBy(staffRepository.getReferenceById(staffId));
        return outbound.getId();
    }

    // 주문 취소 시 출고 취소 처리 (출고등록이 된 경우에만)
    @Transactional
    public void cancelOutboundIfExists(Long orderId) {
        deliveryRepository.findByOrderId(orderId).ifPresent(delivery -> {
            outboundRepository.findByDeliveryId(delivery.getId()).ifPresent(outbound -> {
                outbound.updateOutboundStatus(OutboundStatus.CANCEL);
                outboundRepository.save(outbound);
            });
        });
    }
}
