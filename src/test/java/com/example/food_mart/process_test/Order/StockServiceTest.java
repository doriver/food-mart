package com.example.food_mart.process_test.Order;

import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemStorage;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
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
    @DisplayName("상품 아이디로 조회된 재고 중, 보관 목적이 일치하는 재고만 필터링")
    void getItemStockList_test() {
        // given
        Item item = mock(Item.class);
        given(item.getId()).willReturn(1L);
        given(item.getItemStorage()).willReturn(ItemStorage.COLD);

        Stock stock1 = new Stock(10L, WarehousePurpose.COLD, item.getId(), 1L);
        Stock stock2 = new Stock(25L, WarehousePurpose.COLD, item.getId(), 1L);
        Stock stock3 = new Stock(5L, WarehousePurpose.FREEZER, item.getId(), 3L);

        given(stockRepository.findAllByItemId(item.getId()))
            .willReturn(
                    List.of(stock1,stock2,stock3));

        // when
        List<Stock> result = stockService.getItemStockList(item);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.contains(stock1)).isTrue();
        assertThat(result.contains(stock2)).isTrue();
        assertThat(result.contains(stock3)).isFalse();
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