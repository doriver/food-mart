package com.example.food_mart.process_test.Order;

import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemStatus;
import com.example.food_mart.modules.shop.domain.entity.ItemStorage;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.warehouse.application.StockService;
import com.example.food_mart.modules.warehouse.domain.entity.Picking;
import com.example.food_mart.modules.warehouse.domain.entity.PickingStatus;
import com.example.food_mart.modules.warehouse.domain.entity.Stock;
import com.example.food_mart.modules.warehouse.domain.entity.WarehousePurpose;
import com.example.food_mart.modules.warehouse.domain.repository.PickingRepository;
import com.example.food_mart.modules.warehouse.domain.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private PickingRepository pickingRepository;

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
    @DisplayName("주문 결제에서, 창고에 있는 재고 배송대기 상태로")
    void stockToOutPrepare_test() {
        // given
        Long orderId = 5L;
        Map<Long, Integer> itemAndCount = Map.of(2L, 2, 13L, 3);

        Item item01 = new Item("삼겹살", 15000, ItemStorage.COLD, Map.of("가격단위","600g","원산지","국내산"), 3L, null, null, ItemStatus.ACTIVE);
        Item item02 = new Item("콜라", 1100, ItemStorage.COLD, Map.of("용량","500ml","제조사","팹시"),7L, null, null, ItemStatus.ACTIVE);

        given(itemRepository.findById(2L)).willReturn(Optional.of(item01));
        given(itemRepository.findById(13L)).willReturn(Optional.of(item02));

        Stock stock11 = new Stock(10L, WarehousePurpose.COLD, 2L, 1L);
        ReflectionTestUtils.setField(stock11, "id", 1L);
        Stock stock12 = new Stock(25L, WarehousePurpose.COLD, 2L, 3L);
        ReflectionTestUtils.setField(stock12, "id", 2L);
        Stock stock21 = new Stock(23L, WarehousePurpose.COLD, 13L, 3L);
        ReflectionTestUtils.setField(stock21, "id", 3L);
        doReturn(List.of(stock11, stock12))
                .when(stockService).getItemStockList(item01);
        doReturn(List.of(stock21))
                .when(stockService).getItemStockList(item02);

        given(stockRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(stock11));
//        given(stockRepository.findByIdWithPessimisticLock(2L)).willReturn(Optional.of(stock12));
        given(stockRepository.findByIdWithPessimisticLock(3L)).willReturn(Optional.of(stock21));

        // when
        stockService.stockToOutPrepare(itemAndCount, orderId);

        // then
        ArgumentCaptor<List<Stock>> stocklistCaptor = ArgumentCaptor.forClass(List.class);
        verify(stockRepository).saveAll(stocklistCaptor.capture());
        List<Stock> stockcapturedList = stocklistCaptor.getValue();
        assertThat(stockcapturedList).extracting("id","count").containsExactlyInAnyOrder(
                tuple(1L, 8L),
                tuple(3L, 20L)
        );

        ArgumentCaptor<List<Picking>> pickinglistCaptor = ArgumentCaptor.forClass(List.class);
        verify(pickingRepository).saveAll(pickinglistCaptor.capture());
        List<Picking> pickingcapturedList = pickinglistCaptor.getValue();
        assertThat(pickingcapturedList).extracting("stockId","count").containsExactlyInAnyOrder(
                tuple(1L, 2L),
                tuple(3L, 3L)
        );
    }

    @Test
    @DisplayName("주문 취소 시, Picking 기준으로 재고 수량 복원 + Picking 삭제")
    void restoreStockFromPickings_success() {
        // given
        Long orderId = 5L;

        Picking picking1 = new Picking(orderId, 1L, 5L, PickingStatus.READY, 1L);
        Picking picking2 = new Picking(orderId, 2L, 3L, PickingStatus.READY, 1L);

        Stock stock1 = new Stock(10L, WarehousePurpose.COLD, 100L, 1L);
        ReflectionTestUtils.setField(stock1, "id", 1L);
        Stock stock2 = new Stock(20L, WarehousePurpose.COLD, 101L, 1L);
        ReflectionTestUtils.setField(stock2, "id", 2L);

        given(pickingRepository.findAllByOrderId(orderId)).willReturn(List.of(picking1, picking2));
        given(stockRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(stock1));
        given(stockRepository.findByIdWithPessimisticLock(2L)).willReturn(Optional.of(stock2));

        // when
        stockService.restoreStockFromPickings(orderId);

        // then
        ArgumentCaptor<List<Stock>> stockCaptor = ArgumentCaptor.forClass(List.class);
        verify(stockRepository).saveAll(stockCaptor.capture());
        assertThat(stockCaptor.getValue()).extracting("id", "count").containsExactlyInAnyOrder(
                tuple(1L, 15L),  // 10 + 5
                tuple(2L, 23L)   // 20 + 3
        );
        verify(pickingRepository).deleteAll(List.of(picking1, picking2));
    }

    @Test
    @DisplayName("Picking이 없는 경우 재고 복원 없이 종료")
    void restoreStockFromPickings_noPickings() {
        // given
        Long orderId = 99L;
        given(pickingRepository.findAllByOrderId(orderId)).willReturn(Collections.emptyList());

        // when
        stockService.restoreStockFromPickings(orderId);

        // then
        verify(stockRepository, never()).findByIdWithPessimisticLock(any());
        verify(stockRepository).saveAll(Collections.emptyList());
        verify(pickingRepository).deleteAll(Collections.emptyList());
    }
}