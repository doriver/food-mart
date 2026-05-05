package com.example.food_mart.modules.shop.presentataion.dto.response;

import com.example.food_mart.modules.shop.domain.entity.Item;
import com.example.food_mart.modules.shop.domain.entity.ItemSalesCount;
import com.example.food_mart.modules.shop.domain.entity.ItemStorage;

/**
 * 인기상품 조회 응답 DTO
 */
public record PopularItemResponse(
        Long itemId,
        String name,
        int price,
        ItemStorage itemStorage,
        String imagePath, // 서버에 저장된 파일명 앞에 정적 리소스 경로(/uploads/items/)를 붙여 반환
        Long categoryId,
        long salesCount   // period에 따라 전체기간 or 최근 7일 판매합계
) {
    /**
     * ItemSalesCount(집계 행) + Item(상품 정보)를 합쳐 응답 객체 생성
     */
    public static PopularItemResponse from(ItemSalesCount s, Item item, String period) {
        long salesCount = "WEEKLY".equalsIgnoreCase(period) ? s.getWeeklyCount() : s.getTotalCount();
        String imagePath = item.getImagePath() != null ? "/uploads/items/" + item.getImagePath() : null;
        return new PopularItemResponse(
                item.getId(), item.getName(), item.getPrice(),
                item.getItemStorage(), imagePath, item.getCategoryId(), salesCount
        );
    }
}
