package com.example.food_mart.modules.order.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderFlowMapper {

    OrderFlowRow findOrderFlow(@Param("orderId") Long orderId);
}
