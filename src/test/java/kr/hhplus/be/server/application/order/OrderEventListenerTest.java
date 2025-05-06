package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
class OrderEventListenerTest {

    @Test
    void paymentCompletedEvent_shouldMarkOrderConfirmed() {
        // given
        String orderId = "ORDER123";
        Order order = mock(Order.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        OrderEventListener listener = new OrderEventListener(orderRepository);

        // when
        listener.handlePaymentCompleted(new PaymentCompletedEvent(orderId));

        // then
        verify(order).markConfirmed();
        verify(orderRepository).save(order);
    }
}