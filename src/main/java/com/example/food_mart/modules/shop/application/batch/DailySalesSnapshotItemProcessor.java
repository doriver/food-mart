package com.example.food_mart.modules.shop.application.batch;

import com.example.food_mart.modules.order.domain.mapper.DailySalesRow;
import com.example.food_mart.modules.shop.domain.entity.ItemDailySalesSnapshot;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailySalesSnapshotItemProcessor implements ItemProcessor<DailySalesRow, ItemDailySalesSnapshot> {

    private final LocalDate salesDate;
    private final LocalDateTime snapshotAt;

    public DailySalesSnapshotItemProcessor(LocalDate salesDate, LocalDateTime snapshotAt) {
        this.salesDate = salesDate;
        this.snapshotAt = snapshotAt;
    }

    @Override
    public ItemDailySalesSnapshot process(DailySalesRow row) {
        return new ItemDailySalesSnapshot(
                row.itemId(),
                row.itemName(),
                row.salesCount(),
                salesDate,
                snapshotAt
        );
    }
}
