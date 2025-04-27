package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.outbox.EventStatus;
import kr.hhplus.be.server.domain.outbox.OrderEvent;
import kr.hhplus.be.server.domain.outbox.OrderEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static kr.hhplus.be.server.common.vo.Money.wons;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderEventServiceTest {

    private OrderEventRepository repository;
    private OrderEventService service;

    @BeforeEach
    void setUp() {
        repository = mock(OrderEventRepository.class);
        service = new OrderEventService(repository);
    }

    @Test
    @DisplayName("결제 완료 이벤트를 생성하고 저장한다")
    void record_payment_completed_event_success() {
        // given
        Order order = Order.create(
                1L,
                List.of(OrderItem.of(100L, 2, 260, wons(10000))),
                wons(20000)
        );

        // when
        service.recordPaymentCompletedEvent(order);

        // then
        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(repository, times(1)).save(captor.capture());

        OrderEvent savedEvent = captor.getValue();
        assertThat(savedEvent).isNotNull();
        assertThat(savedEvent.getAggregateType()).isEqualTo("ORDER");
        assertThat(savedEvent.getEventType()).isEqualTo("PAYMENT_COMPLETED");
        assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.PENDING);
        assertThat(savedEvent.getPayload()).contains(order.getId());
    }
}
