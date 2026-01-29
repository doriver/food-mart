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
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemApiController {

    private final ItemService itemService;

    /*
        todo : 재고(Stock)에 있는 물건을 item으로 등록할수 있도록할꺼, 아직 재고쪽은 미개발
        아이템 등록( 마트에서 판매할 )
     */
    @PostMapping
    public ApiResponse<Long> registerItem(@RequestBody ItemCreateDTO itemCreateDTO, UserInfo userInfo) {
        Long registeredItemId = itemService.registerItem(itemCreateDTO, userInfo);

        return ApiResponse.success(registeredItemId);
    }

}
