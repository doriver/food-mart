package com.example.food_mart.process_test.Order;

import com.example.food_mart.modules.logistic.application.OutboundService;
import com.example.food_mart.modules.logistic.domain.entity.Delivery;
import com.example.food_mart.modules.logistic.domain.entity.DeliveryCompany;
import com.example.food_mart.modules.logistic.domain.entity.Outbound;
import com.example.food_mart.modules.logistic.domain.entity.OutboundStatus;
import com.example.food_mart.modules.logistic.domain.repository.DeliveryRepository;
import com.example.food_mart.modules.logistic.domain.repository.OutboundRepository;
import com.example.food_mart.modules.warehouse.domain.repository.PickingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboundServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private OutboundRepository outboundRepository;
    @Mock
    private PickingRepository pickingRepository;

    @InjectMocks
    private OutboundService outboundService;

    @Test
    @DisplayName("출고가 등록된 경우, Outbound 상태를 CANCEL로 변경")
    void cancelOutboundIfExists_outboundExists() {
        // given
        Long orderId = 1L;

        Delivery delivery = new Delivery(orderId, "서울시 강남구", DeliveryCompany.CJ, "TRACK001");
        ReflectionTestUtils.setField(delivery, "id", 10L);

        Outbound outbound = new Outbound(1L, delivery.getId(), OutboundStatus.READY);
        ReflectionTestUtils.setField(outbound, "id", 20L);

        given(deliveryRepository.findByOrderId(orderId)).willReturn(Optional.of(delivery));
        given(outboundRepository.findByDeliveryId(delivery.getId())).willReturn(Optional.of(outbound));

        // when
        outboundService.cancelOutboundIfExists(orderId);

        // then
        assertThat(outbound.getOutboundStatus()).isEqualTo(OutboundStatus.CANCEL);
        verify(outboundRepository).save(outbound);
    }

    @Test
    @DisplayName("Delivery 자체가 없는 경우 (출고 미등록), 아무 처리 없이 종료")
    void cancelOutboundIfExists_noDelivery() {
        // given
        Long orderId = 3L;
        given(deliveryRepository.findByOrderId(orderId)).willReturn(Optional.empty());

        // when
        outboundService.cancelOutboundIfExists(orderId);

        // then
        verify(outboundRepository, never()).findByDeliveryId(any());
        verify(outboundRepository, never()).save(any());
    }
}
