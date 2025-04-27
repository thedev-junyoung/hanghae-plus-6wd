package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderService(orderRepository);
    }

    @Test
    @DisplayName("주문을 생성할 수 있다")
    void createOrder_success() {
        // given
        Long userId = 1L;
        List<OrderItem> items = List.of(OrderItem.of(101L, 2, 260, Money.wons(10000)));
        Money total = Money.wons(20000);

        // when
        Order result = orderService.createOrder(userId, items, total);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalAmount()).isEqualTo(total.value());

        verify(orderRepository).save(result);
    }

    @Test
    @DisplayName("결제 가능한 주문을 조회할 수 있다")
    void getOrderForPayment_success() {
        // given
        String orderId = UUID.randomUUID().toString();
        Order mockOrder = mock(Order.class);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // when
        Order result = orderService.getOrderForPayment(orderId);

        // then
        assertThat(result).isEqualTo(mockOrder);
        verify(mockOrder).validatePayable();
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 예외 발생")
    void getOrderForPayment_notFound() {
        // given
        String orderId = "non-existent";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderForPayment(orderId))
                .isInstanceOf(OrderException.NotFoundException.class)
                .hasMessageContaining(orderId);
    }

    @Test
    @DisplayName("주문을 CONFIRMED 상태로 변경할 수 있다")
    void markConfirmed_success() {
        // given
        Order order = mock(Order.class);

        // when
        orderService.markConfirmed(order);

        // then
        verify(order).markConfirmed();
        verify(orderRepository).save(order);
    }
}
