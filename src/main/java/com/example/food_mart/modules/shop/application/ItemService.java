package com.example.food_mart.modules.shop.application;

import com.example.food_mart.common.FileStorageService;
import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.shop.domain.entity.Category;
import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemSalesCount;
import com.example.food_mart.modules.shop.domain.entity.ItemStatus;
import com.example.food_mart.modules.shop.domain.repository.CategoryRepository;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.shop.domain.repository.ItemSalesCountRepository;
import com.example.food_mart.modules.shop.presentataion.dto.request.ItemCreateDTO;
import com.example.food_mart.modules.shop.presentataion.dto.response.ItemResponse;
import com.example.food_mart.modules.shop.presentataion.dto.response.PopularItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final ItemSalesCountRepository itemSalesCountRepository;

    /*
        카테고리 등록
     */
    public Long registerCategory(String name, Long parentId) {
        Category category = new Category(name, parentId);
        Category savedCategory = categoryRepository.save(category);
        return savedCategory.getId();
    }


    public ItemResponse getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Expected4xxException(ErrorCode.ITEM_NOT_FOUND));
        if (item.getStatus() == ItemStatus.HIDDEN) {
            throw new Expected4xxException(ErrorCode.ITEM_NOT_FOUND);
        }
        return ItemResponse.from(item);
    }

    /**
     * 인기상품 조회 (2단계 조회 방식)
     *
     * 1단계 — ItemSalesCount 집계 테이블에서 페이지 조회
     *   - HIDDEN 상품은 스케줄러(ItemSalesCountSyncScheduler)가 집계 테이블에서 미리 삭제하므로
     *     여기서 별도 status 필터 불필요
     */
    public Page<PopularItemResponse> getPopularItems(Long categoryId, String period, Pageable pageable) {
        String sortField = "WEEKLY".equalsIgnoreCase(period) ? "weeklyCount" : "totalCount";
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(sortField).descending()); // period 값에따라 정렬 필드 다르게

        // 1단계: ItemSalesCount 집계 테이블에서 페이지 조회
        Page<ItemSalesCount> salesPage = (categoryId != null)
                ? itemSalesCountRepository.findByCategoryId(categoryId, sorted)
                : itemSalesCountRepository.findAll(sorted);

        // 2단계: itemId 목록으로 Item을 IN절 일괄 조회(페이지 크기만큼만 조회하므로 성능 부담 없음)
        List<Long> itemIds = salesPage.map(ItemSalesCount::getItemId).toList();
        Map<Long, Item> itemMap = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, i -> i));

        return salesPage.map(s -> PopularItemResponse.from(s, itemMap.get(s.getItemId()), period));
    }

    public Page<ItemResponse> getItemList(Long categoryId, Pageable pageable) {
        List<ItemStatus> visibleStatuses = List.of(ItemStatus.ACTIVE, ItemStatus.SOLDOUT);
        Page<Item> items = (categoryId != null)
                ? itemRepository.findByCategoryIdAndStatusIn(categoryId, visibleStatuses, pageable)
                : itemRepository.findByStatusIn(visibleStatuses, pageable);
        return items.map(ItemResponse::from);
    }

    /*
        아이템 등록
     */
    public Long registerItem(ItemCreateDTO itemCreateDTO, MultipartFile image, UserInfo userInfo) {
//        UserUtils.checkLogin(userInfo.getUserId());
//        UserUtils.checkManagerAdmin(userInfo.getRole());
        String imagePath = (image != null && !image.isEmpty()) ? fileStorageService.store(image) : null;
        ItemStatus status = itemCreateDTO.getStatus() != null ? itemCreateDTO.getStatus() : ItemStatus.ACTIVE;
        Item item = new Item(itemCreateDTO.getName(), itemCreateDTO.getPrice(), itemCreateDTO.getItemStorage(), itemCreateDTO.getAttribute(), itemCreateDTO.getCategoryId(),
                itemCreateDTO.getDescription(), imagePath, status);
        Item savedItem = itemRepository.save(item);
        return savedItem.getId();
    }

}
