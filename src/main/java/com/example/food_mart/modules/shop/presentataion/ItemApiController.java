package com.example.food_mart.modules.shop.presentataion;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.modules.shop.application.ItemService;
import com.example.food_mart.modules.shop.presentataion.dto.request.ItemCreateDTO;
import com.example.food_mart.modules.shop.presentataion.dto.response.ItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Operation(summary = "아이템 카테고리 등록")
    @PostMapping("/categories")
    public ApiResponse<Long> registerCategory(@RequestBody CategoryCreateDTO dto, UserInfo userInfo) {
        Long registeredCategoryId
                = itemService.registerCategory(dto.name(), dto.parentId());
        return ApiResponse.success(registeredCategoryId);
    }


    @Operation(summary = "상품 상세 조회")
    @GetMapping("/items/{itemId}")
    public ApiResponse<ItemResponse> getItem(@PathVariable Long itemId) {
        return ApiResponse.success(itemService.getItem(itemId));
    }

    @Operation(summary = "상품 목록 조회")
    @GetMapping("/items")
    public ApiResponse<Page<ItemResponse>> getItemList(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(itemService.getItemList(categoryId, pageable));
    }

    /*
        아이템 등록(마트에서 판매할)
        @param: 이름, 가격, 보관방법, 세부속성, 카테고리
     */
    @Operation(summary = "아이템 등록")
    @PostMapping(value = "/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Long> registerItem(
            @RequestPart("item") ItemCreateDTO itemCreateDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            UserInfo userInfo) {
        Long registeredItemId = itemService.registerItem(itemCreateDTO, image, userInfo);
        return ApiResponse.success(registeredItemId);
    }
}
