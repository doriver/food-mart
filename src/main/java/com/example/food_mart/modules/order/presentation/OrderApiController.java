package com.example.food_mart.modules.order.presentation;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.modules.order.application.OrderReadService;
import com.example.food_mart.modules.order.application.OrderService;
import com.example.food_mart.modules.order.presentation.dto.request.OrderCreateDTO;
import com.example.food_mart.modules.order.presentation.dto.response.OrderDetailDTO;
import com.example.food_mart.modules.order.presentation.dto.response.OrderReadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;
    private final OrderReadService orderReadService;

    /*
        주문하기 API
        @param: 배송주소
     */
    @PostMapping
    public ApiResponse<Long> processOrder(@RequestBody OrderCreateDTO orderCreateDTO, UserInfo userInfo) {
        Long savedOrderId = orderService.processOrder(orderCreateDTO, userInfo);
        return ApiResponse.success(savedOrderId);
    }

    /*
        주문내역 조회 API
        회원의 주문 목록
     */
    @GetMapping
    public ApiResponse<List<OrderReadDTO>> readOrders(UserInfo userInfo) {
        List<OrderReadDTO> orderList = orderReadService.readOrders(userInfo.getUserId());
        return ApiResponse.success(orderList);
    }


    /*
        주문상세 조회 API
        Order, OrderItem, Delivery, 이 외 정보
     */
    @GetMapping("/{id}")
    public ApiResponse<OrderDetailDTO> readOrderDetail(@PathVariable("id") Long orderId, UserInfo userInfo) {
        OrderDetailDTO orderDetailDTO = orderReadService.readOrderDetail(userInfo.getUserId(), orderId);
        return ApiResponse.success(orderDetailDTO);
    }
}
