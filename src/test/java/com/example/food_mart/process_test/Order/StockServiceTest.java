package com.example.food_mart.process_test.Order;

import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.warehouse.application.StockService;
import com.example.food_mart.modules.warehouse.domain.entity.Stock;
import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
import com.example.food_mart.modules.warehouse.domain.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Spy
    @InjectMocks
    private StockService stockService;

    @Test
    void getItemStockList_test() {
    }

    @Test
    @DisplayName("특정 item의 재고 개수 파악")
    void countStockForItem_test() {
    // given
        Item item = mock(Item.class);

        // getItemStockList 메서드가 호출될 때 위 리스트를 반환하도록 Mocking
        // (doReturn을 사용하는 이유는 spy 객체의 실제 메서드 호출을 방지하기 위함입니다)
        doReturn(List.of(
                    new Stock(10L, WarehousePurpose.COLD, item.getId(), 1L)
                    , new Stock(25L, WarehousePurpose.COLD, item.getId(), 1L)
                    , new Stock(5L, WarehousePurpose.COLD, item.getId(), 3L)
                ))
                .when(stockService).getItemStockList(item);

    // when
        Long totalStock = stockService.countStockForItem(item);

    // then
        assertThat(totalStock).isEqualTo(10L + 25L + 5L);
    }

    @Test
    void stockToOutPrepare_test() {
    }
}