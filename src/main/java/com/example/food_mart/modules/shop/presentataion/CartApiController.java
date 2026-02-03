package com.example.food_mart.modules.shop.presentataion;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.modules.shop.application.CartService;
import com.example.food_mart.modules.shop.presentataion.dto.request.ItemInCartCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    /*
        장바구니에 아이템 담기
        @param: 아이템id, 개수
     */
    @PostMapping
    public ApiResponse<Long> saveItemInCart(@RequestBody ItemInCartCreateDTO itemInCartCreateDTO, UserInfo userInfo) {
        Long savedCartId = cartService.saveItemInCart(itemInCartCreateDTO, userInfo.getUserId());
        return ApiResponse.success(savedCartId);
    }
}
