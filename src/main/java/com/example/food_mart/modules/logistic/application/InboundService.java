package com.example.food_mart.modules.logistic.application;

import com.example.food_mart.modules.logistic.domain.entity.Inbound;
import com.example.food_mart.modules.logistic.domain.entity.InboundItem;
import com.example.food_mart.modules.logistic.domain.entity.InboundStackingStatus;
import com.example.food_mart.modules.logistic.domain.repository.InboundItemRepository;
import com.example.food_mart.modules.logistic.domain.repository.InboundRepository;
import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.staff.application.StaffConstants;
import com.example.food_mart.modules.staff.domain.Staff;
import com.example.food_mart.modules.staff.domain.StaffRepository;
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
    private final StaffRepository staffRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Long registerInbound(Long staffId, String supplier, Map<Long,Long> itemAndCount) {
        Staff staff = staffRepository.getReferenceById(staffId);
        Inbound savedInbound = inboundRepository.save(new Inbound(staff, supplier));

        Staff stackingStaff = staffRepository.getReferenceById(StaffConstants.stackingMaster);
        List<InboundItem> inboundItemList = new ArrayList<>();
        itemAndCount.forEach((itemId, count) -> {
            Item item = itemRepository.getReferenceById(itemId);
            inboundItemList.add(new InboundItem(savedInbound, item, count, stackingStaff, InboundStackingStatus.READY));
        });
        inboundItemRepository.saveAll(inboundItemList);
        return savedInbound.getId();
    }

}
