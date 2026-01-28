package com.example.food_mart.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
/*
    자주사용되는 ExpectedException들 정리및 관리
    현재있는 List들은 정리 필요
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 주문
    FAIL_ORDER(HttpStatus.INTERNAL_SERVER_ERROR, "주문에 실패했습니다.")
    , FAIL_ORDER_ITEM(HttpStatus.INTERNAL_SERVER_ERROR, "아이템 주문에 실패했습니다.")
    , FAIL_SAVE_DELIVERY(HttpStatus.INTERNAL_SERVER_ERROR, "배송정보 입력 실패했습니다.")
    , FAIL_TRANSACTION(HttpStatus.INTERNAL_SERVER_ERROR, "상품 결제에 실패했습니다.")
    , FAIL_ITEM_COUNT_DOWN_ORDER(HttpStatus.INTERNAL_SERVER_ERROR, "주문에 따른 상품개수 변화 실패")
    , FAIL_EMPTY_CART(HttpStatus.INTERNAL_SERVER_ERROR, "장바구니 비우기에 실패했습니다.")
    , NOT_FOUND_ORDER(HttpStatus.BAD_REQUEST, "해당 주문은 존재하지 않습니다.")

    // 주문 아이템
    , NOT_FOUND_ORDERITEM(HttpStatus.INTERNAL_SERVER_ERROR, "주문아이템을 찾을수 없습니다.")

    // 배송
    , NOT_FOUND_DELIVERY(HttpStatus.INTERNAL_SERVER_ERROR, "배송정보를 찾을수 없습니다.")
    
    // 장바구니
    , FAIL_SAVE_CART(HttpStatus.INTERNAL_SERVER_ERROR, "아이템을 장바구니에 담기 실패했습니다.")
    , ZERO_CART(HttpStatus.BAD_REQUEST, "장바구니가 비였습니다.")

    // 아이템
    , ITEM_SOLD_OUT(HttpStatus.BAD_REQUEST, "해당아이템은 품절되었습니다")
    , ITEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당아이템은 존재하지 않습니다.")
    , ITEM_OUTOF_STOCK(HttpStatus.BAD_REQUEST, "해당아이템의 제고가 부족합니다.")

    // 유저 지갑
    , WALLET_NOT_FOUND(HttpStatus.BAD_REQUEST, "지갑이 존재하지 않습니다.")
    , DONT_BUY_MONEY(HttpStatus.BAD_REQUEST, "보유하신 금액이 모잘라 구매할수 없습니다.")
    , NOT_ENOUGH_MONEY(HttpStatus.BAD_REQUEST, "보유하신 금액이 부족합니다.")

    // 유저
    , NEED_TO_LOGIN(HttpStatus.BAD_REQUEST, "로그인이 필요합니다.")
    , DONT_HAVE_AUTHORITY(HttpStatus.BAD_REQUEST, "해당 서비스를 이용할 권한이 없습니다.")

    ;
    private final HttpStatus httpStatus;
    private final String message;
}
