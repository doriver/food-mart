package com.example.food_mart.modules.warehouse.application;

import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.logistic.domain.entity.InboundItem;
import com.example.food_mart.modules.logistic.domain.entity.InboundStackingStatus;
import com.example.food_mart.modules.logistic.domain.repository.InboundItemRepository;
import com.example.food_mart.modules.warehouse.domain.entity.Stock;
import com.example.food_mart.modules.warehouse.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StackingService {

    private final InboundItemRepository inboundItemRepository;
    private final StockRepository stockRepository;

    @Transactional
    public void doCompleteStacking(Long inboundItemId, Long stackingStaffId, Map<Long,Long> stockAndCount) {
        InboundItem inboundItem = inboundItemRepository.findById(inboundItemId)
                .orElseThrow(() -> new Expected4xxException("해당 입고아이템은 없습니다."));
        inboundItem.updateStackingStaffId(stackingStaffId);

        List<Stock> stockList = new ArrayList<>();
        Stock changedStock = null;
        for (Long stockId : stockAndCount.keySet()) {
            changedStock = stockRepository.findByIdWithPessimisticLock(stockId)
                    .orElseThrow(() -> new Expected4xxException("해당 재고는 없습니다"));
            changedStock.plusCount(stockAndCount.get(stockId));
            stockList.add(changedStock);
        }
        stockRepository.saveAll(stockList);

        inboundItem.updateInboundStackingStatus(InboundStackingStatus.COMPLETED);
        inboundItemRepository.save(inboundItem);

    }
}
