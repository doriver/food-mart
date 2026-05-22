package com.example.food_mart.modules.logistic.domain.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OutboundDelayMapper {

    List<OutboundDelayRow> findOutboundDelays();
}
