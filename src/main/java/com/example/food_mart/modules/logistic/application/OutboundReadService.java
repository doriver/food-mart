package com.example.food_mart.modules.logistic.application;

import com.example.food_mart.modules.logistic.domain.mapper.OutboundDelayMapper;
import com.example.food_mart.modules.logistic.domain.mapper.OutboundDelayRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundReadService {

    private final OutboundDelayMapper outboundDelayMapper;

    public List<OutboundDelayRow> getOutboundDelays() {
        return outboundDelayMapper.findOutboundDelays();
    }
}
