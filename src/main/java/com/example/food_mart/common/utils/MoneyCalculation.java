package com.example.food_mart.common.utils;

public class MoneyCalculation {

    /*
        아이템 총 가격 구하기
        아이템 가격 * 아이템 개수
    */
    public static long priceCount(int itemPrice, int itemCount) {
        return (long) itemPrice * itemCount;
    }
}
