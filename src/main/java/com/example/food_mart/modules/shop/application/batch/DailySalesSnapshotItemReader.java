package com.example.food_mart.modules.shop.application.batch;

import com.example.food_mart.modules.order.domain.mapper.DailySalesRow;
import com.example.food_mart.modules.order.domain.mapper.OrderItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemReader;

import java.time.LocalDateTime;
import java.util.Iterator;

@RequiredArgsConstructor
public class DailySalesSnapshotItemReader implements ItemReader<DailySalesRow> {

    private final OrderItemMapper orderItemMapper;
    private final LocalDateTime from;
    private final LocalDateTime to;
    private Iterator<DailySalesRow> iterator;

    @Override
    public DailySalesRow read() {
        if (iterator == null) {
            iterator = orderItemMapper.sumDailyCountByItemId(from, to).iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}
