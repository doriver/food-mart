package com.example.food_mart.modules.order.application;

import com.example.food_mart.common.argumentResolver.UserInfo;
import com.example.food_mart.common.exception.ErrorCode;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.order.presentation.dto.request.OrderCreateDTO;
import com.example.food_mart.modules.shop.application.CartService;
import com.example.food_mart.modules.shop.domain.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final DeliveryService deliveryService;
    private final CartService cartService;
    private final CartBeforeOrderService cartBeforeOrderService;
    private final TransactionService transactionService;

    /*
        주문 절차
        1. 도메인 객체 Cart생성, 장바구니에 있는 아이템들 담기
        2. Cart에 있는 상품들 구매할수 있는지 판단
        3. 주문하기
        4. 장바구니 비우기( 비동기로 처리하면 좋을듯 )
     */
    public Long processOrder(OrderCreateDTO orderCreateDTO, UserInfo userInfo) {
        
//        UserUtils.checkLogin(userInfo.getUserId());

        // 도메인 객체 생성, 장바구니에 있는 아이템들 담기
        Cart cart = new Cart();
        cartService.setItemListInCart(userInfo.getUserId(), cart);

        // 구매할수 있는지 판단( 돈, 개수 )
        judgeBuyable(userInfo.getUserId(), cart);

        // 주문하기
        Long savedOrderId = createOrder(userInfo.getUserId(), orderCreateDTO.getAddress(), cart);

        // 장바구니 비우기
        cartService.emptyCartAfterOrder(userInfo.getUserId(), cart);
        return savedOrderId;
    }

    /*
        주문자의 장바구니에 있는 상품들 구매할수 있는지 판단
        1. 돈    2. Stock 개수
     */
    public void judgeBuyable(Long userId, Cart cart) {

        // 총 가격과 있는돈 비교하기
        cart.calculateTotalPrice();
        boolean buyablePrice = cartBeforeOrderService.buyablePrice(userId, cart.getTotalPrice());
        if (! buyablePrice) {
            throw new Expected4xxException(ErrorCode.DONT_BUY_MONEY);
        }

        // Stock 개수 확인하기
        String result = cartBeforeOrderService.buyableCount(cart);
        if (! result.equals("ok")) {
            throw new Expected4xxException(result);
        }
    }


    /*
        주문하기
        1.배송정보 저장    2.Order생성    3.OrderItem들 저장
     */
    public Long createOrder(Long userId, String address, Cart cart) {

        // 배송정보 저장
        Long savedDeliveryId = deliveryService.saveDelivery(address);

        // 2 ~ 5
        Long savedOrderId = transactionService.order(userId, savedDeliveryId, cart);

        return savedOrderId;
    }

    /*
        주문 결제
        1.돈 결제    2.Stock 배송 대기상태
     */

}
