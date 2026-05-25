package com.example.food_mart.modules.order.domain.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderItemMapper {

    List<WeeklySalesRow> sumWeeklyCountByItemId(@Param("since") LocalDateTime since,
                                                @Param("statuses") List<String> statuses);

    List<DailySalesRow> sumDailyCountByItemId(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);
}
