package com.example.food_mart.modules.shop.application;

import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.shop.presentataion.dto.request.ItemCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    /*
        todo : 재고(Stock)에 있는 물건을 item으로 등록할수 있도록할꺼, 아직 재고쪽은 미개발
        아이템 등록
     */
    public Long registerItem(ItemCreateDTO itemCreateDTO, UserInfo userInfo) {
//        UserUtils.checkLogin(userInfo.getUserId());
//        UserUtils.checkManagerAdmin(userInfo.getRole());

        // Item저장
        Item item = new Item(itemCreateDTO.getName(), itemCreateDTO.getPrice(), itemCreateDTO.getCount(), LocalDateTime.now());
        Item savedItem = itemRepository.save(item);
        return savedItem.getId();
    }
    
    /*
        주문에따른, Item들 개수차감
        구매 수량만큼 -
     */
    public void itemCountDownByOrder(Map<Long, Integer> itemAndCountMap) {
        List<Item> items = new ArrayList<>();

        for (Long itemId : itemAndCountMap.keySet()) {
            Item item = itemRepository.findById(itemId).orElse(null); // 앞쪽 judgeBuyable()에서 이미 검증됨
            item.minusCount(itemAndCountMap.get(itemId));
            items.add(item);
        }
        try {
            itemRepository.saveAll(items);
        } catch (Exception e) {
            throw new Expected4xxException(ErrorCode.FAIL_ITEM_COUNT_DOWN_ORDER);
        }

    }
}
