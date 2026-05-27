package com.example.food_mart.modules.warehouse.application;

import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.common.exception.Expected5xxException;
import com.example.food_mart.modules.order.domain.entity.OrderStatus;
import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemStorage;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.staff.application.StaffConstants;
import com.example.food_mart.modules.order.domain.entity.Order;
import com.example.food_mart.modules.order.domain.repository.OrderRepository;
import com.example.food_mart.modules.staff.domain.Staff;
import com.example.food_mart.modules.staff.domain.StaffRepository;
import com.example.food_mart.modules.warehouse.domain.entity.*;
import com.example.food_mart.modules.warehouse.domain.repository.PickingRepository;
import com.example.food_mart.modules.warehouse.domain.repository.StockRepository;
import com.example.food_mart.modules.warehouse.domain.repository.WarehouseRepository;
import com.example.food_mart.modules.warehouse.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderRepository orderRepository;
    private final StaffRepository staffRepository;
    private final PickingRepository pickingRepository;
    private final StockRepository stockRepository;

    // 재고 등록
    public Long registerStock(Long count, WarehousePurpose locationType, Long itemId, Long warehouseId) {
        Item item = itemRepository.getReferenceById(itemId);
        Warehouse warehouse = warehouseRepository.getReferenceById(warehouseId);
        Stock saved = stockRepository.save(new Stock(count, locationType, item, warehouse));
        return saved.getId();
    }

    // 특정 item의 stock들 파악
    public List<Stock> getItemStockList(Item item) {
        List<Stock> stockList = stockRepository.findAllByItemId(item.getId());
        if (stockList.isEmpty()) {
            throw new Expected4xxException(item.getName() + "의 재고가 없습니다.");
        }

        List<Stock> filteredstockList = stockList.stream()
                .filter(stock -> stock.getLocationType()
                        == item.getItemStorage().toWarehousePurpose())
                .toList();
        return filteredstockList;
    }

    // 특정 item의 재고 개수 파악
    public Long countStockForItem(Item item) {
        List<Stock> stockList = getItemStockList(item);

        Long totalStock = stockList.stream()
                .mapToLong(Stock::getCount)
                .sum();
        return totalStock;
    }


    // 창고에 있는 재고, 배송대기 상태로
    @Transactional
    public void stockToOutPrepare(Map<Long, Integer> itemAndCount, Order order) {
        List<Stock> changedStockList = new ArrayList<>();
        List<Picking> pickingList = new ArrayList<>();

        Order orderRef = orderRepository.getReferenceById(order.getId());
        Staff staffRef = staffRepository.getReferenceById(StaffConstants.pickingMaster);

        for (Long itemId : itemAndCount.keySet()) {
        // 1. 각 아이템 재고찾기
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new Expected4xxException(ErrorCode.ITEM_NOT_FOUND));
            List<Stock> stockList = getItemStockList(item);

        // 2. Stock 배송대기 상태로( 재고 감소 + 오더 피킹 생성 )
            Integer itemCount = itemAndCount.get(itemId);
            long remainOrderItem = itemCount;

            for (Stock stock :stockList) {
                // 락을 걸어서 조회(Stock들의 id로 다시 조회)한다음에, 개수빼고 업데이트( 창고가 많아지는 경우는, 어디서부터 뺄지 결정하는것도 중요해질수 있음 )
                Stock lockedStock = stockRepository.findByIdWithPessimisticLock(stock.getId())
                        .orElseThrow(() -> new Expected5xxException("서버측 에러 발생"));
                
                long pickingCount // 재고에서 실 차감 개수
                        = Math.min(remainOrderItem, lockedStock.getCount());

                remainOrderItem = lockedStock.minusCount(pickingCount);
                changedStockList.add(lockedStock);

                // 오더 피킹
                pickingList.add(new Picking(orderRef, lockedStock, pickingCount, PickingStatus.READY, staffRef));

                if (remainOrderItem == 0) {
                    break;
                }
            }
        }

        // 주문 상태 업데이트
        order.updateStatus(OrderStatus.WAITDELIVERY);
        
        try {
            stockRepository.saveAll(changedStockList);
            pickingRepository.saveAll(pickingList);
            orderRepository.save(order);
        } catch (Exception e) {
            throw new Expected5xxException("주문하신 상품의 배송대기가 실패했습니다.");
        }
    }

    // 주문 취소 시 재고 복원
    @Transactional
    public void restoreStockFromPickings(Long orderId) {
        List<Picking> pickingList = pickingRepository.findAllByOrderId(orderId);

        List<Stock> changedStockList = new ArrayList<>();
        for (Picking picking : pickingList) {
            Stock lockedStock = stockRepository.findByIdWithPessimisticLock(picking.getStockId())
                    .orElseThrow(() -> new Expected5xxException("서버측 에러 발생"));
            lockedStock.plusCount(picking.getCount());
            changedStockList.add(lockedStock);
        }

        try {
            stockRepository.saveAll(changedStockList);
            pickingRepository.deleteAll(pickingList);
        } catch (Exception e) {
            throw new Expected5xxException("주문 취소에 따른 재고 복원에 실패했습니다.");
        }
    }
}
