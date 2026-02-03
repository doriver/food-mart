package com.example.food_mart.modules.shop.presentataion;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.modules.shop.application.ItemService;
import com.example.food_mart.modules.shop.presentataion.dto.request.ItemCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemApiController {

    private final ItemService itemService;

    /*
        아이템 카테고리 등록
        @param: 카테고리 이름, 부모 카테고리
     */
    record CategoryCreateDTO(String name, Long parentId) {}

    @PostMapping("/categories")
    public ApiResponse<Long> registerCategory(@RequestBody CategoryCreateDTO dto, UserInfo userInfo) {
        Long registeredCategoryId
                = itemService.registerCategory(dto.name(), dto.parentId());
        return ApiResponse.success(registeredCategoryId);
    }

    /*
        아이템 등록(마트에서 판매할)
        @param: 이름, 가격, 보관방법, 세부속성, 카테고리
     */
    @PostMapping("/items")
    public ApiResponse<Long> registerItem(@RequestBody ItemCreateDTO itemCreateDTO, UserInfo userInfo) {
        Long registeredItemId = itemService.registerItem(itemCreateDTO, userInfo);
        return ApiResponse.success(registeredItemId);
    }
}
