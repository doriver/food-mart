package com.example.food_mart.modules.logistic.application;

import com.example.food_mart.modules.logistic.domain.entity.Inbound;
import com.example.food_mart.modules.logistic.domain.entity.InboundItem;
import com.example.food_mart.modules.logistic.domain.repository.InboundItemRepository;
import com.example.food_mart.modules.logistic.domain.repository.InboundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InboundService {

    private final InboundRepository inboundRepository;
    private final InboundItemRepository inboundItemRepository;

    @Transactional
    public Long registerInbound(Long staffId, String supplier, Map<Long,Long> itemAndCount) {
        Inbound savedInbound = inboundRepository.save(new Inbound(staffId, supplier));
        Long inboundId = savedInbound.getId();

        List<InboundItem> inboundItemList = new ArrayList<>();
        itemAndCount.forEach((key, value) -> {
            inboundItemList.add(
                    new InboundItem(inboundId, key, value));
        });
        inboundItemRepository.saveAll(inboundItemList);
        return inboundId;
    }

}
