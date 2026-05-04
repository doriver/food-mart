package com.example.food_mart.modules.shop.application;

import com.example.food_mart.common.FileStorageService;
import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.modules.shop.domain.entity.Category;
import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemStatus;
import com.example.food_mart.modules.shop.domain.repository.CategoryRepository;
import com.example.food_mart.modules.shop.domain.repository.ItemRepository;
import com.example.food_mart.modules.shop.presentataion.dto.request.ItemCreateDTO;
import com.example.food_mart.modules.shop.presentataion.dto.response.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    /*
        카테고리 등록
     */
    public Long registerCategory(String name, Long parentId) {
        Category category = new Category(name, parentId);
        Category savedCategory = categoryRepository.save(category);
        return savedCategory.getId();
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
