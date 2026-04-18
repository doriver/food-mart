package com.example.food_mart.modules.warehouse.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.warehouse.domain.entity.Picking;
import com.example.food_mart.modules.warehouse.domain.entity.PickingStatus;
import com.example.food_mart.modules.warehouse.domain.repository.PickingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PickingService {

    private final PickingRepository pickingRepository;

    @Transactional
    public Long completePicking(Long pickingId, Long staffId) {
        Picking picking = pickingRepository.findById(pickingId)
                .orElseThrow(() -> new Expected4xxException(ErrorCode.NOT_FOUND_PICKING));

        if (picking.getPickingStatus() == PickingStatus.COMPLETED) {
            throw new Expected4xxException(ErrorCode.ALREADY_COMPLETED_PICKING);
        }

        picking.completeBy(staffId); // dirty checking 으로 update
        return picking.getId();
    }
}
