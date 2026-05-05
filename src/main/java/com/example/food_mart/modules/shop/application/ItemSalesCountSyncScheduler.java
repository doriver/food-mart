package com.example.food_mart.modules.shop.application;

import com.example.food_mart.modules.order.domain.mapper.OrderItemMapper;
import com.example.food_mart.modules.order.domain.mapper.WeeklySalesRow;
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

    private final OrderItemMapper orderItemMapper;
    private final ItemSalesCountRepository itemSalesCountRepository;

    // 매주 일요일 새벽 3시
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void sync() {
        Map<Long, Long> weeklyCountMap = new HashMap<>();
        for (WeeklySalesRow row : orderItemMapper.sumWeeklyCountByItemId(
                LocalDateTime.now().minusDays(7),
                List.of("DELIVERY", "COMPLETE"))) {
            weeklyCountMap.put(row.itemId(), row.cnt());
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
