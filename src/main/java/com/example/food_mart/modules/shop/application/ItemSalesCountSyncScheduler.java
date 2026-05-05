package com.example.food_mart.modules.shop.application;

import com.example.food_mart.modules.order.domain.repository.OrderItemRepository;
import com.example.food_mart.modules.shop.domain.entity.ItemSalesCount;
import com.example.food_mart.modules.shop.domain.repository.ItemSalesCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ItemSalesCountSyncScheduler {

    private final OrderItemRepository orderItemRepository;
    private final ItemSalesCountRepository itemSalesCountRepository;

    // 매주 일요일 새벽 3시
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void sync() {
        Map<Long, Long> weeklyCountMap = new HashMap<>();
        for (Object[] row : orderItemRepository.sumWeeklyCountByItemId(LocalDateTime.now().minusDays(7))) {
            weeklyCountMap.put((Long) row[0], (Long) row[1]);
        }

        List<ItemSalesCount> toUpdate = itemSalesCountRepository.findAllById(weeklyCountMap.keySet());
        toUpdate.forEach(s -> {
            long weekly = weeklyCountMap.get(s.getItemId());
            s.updateWeeklyCount(weekly);
            s.addTotalCount(weekly);
        });
        itemSalesCountRepository.saveAll(toUpdate);
    }
}
