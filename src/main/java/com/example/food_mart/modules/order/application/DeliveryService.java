package com.example.food_mart.modules.order.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.order.domain.entity.Delivery;
import com.example.food_mart.modules.order.domain.entity.DeliveryStatus;
import com.example.food_mart.modules.order.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    /*
        배송정보 입력
     */
    public Long saveDelivery(String address) {
        Delivery delivery = new Delivery(DeliveryStatus.PREPARING, address, LocalDateTime.now());
        Long savedDeliveryId = null;
        try {
            savedDeliveryId = deliveryRepository.save(delivery).getId();
        } catch (Exception e) {
            throw new Expected4xxException(ErrorCode.FAIL_SAVE_DELIVERY);
        }
        return savedDeliveryId;
    }

}
