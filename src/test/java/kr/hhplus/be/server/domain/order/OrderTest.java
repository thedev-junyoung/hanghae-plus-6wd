package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    @DisplayName("정상적으로 주문을 생성할 수 있다")
    void create_order_should_succeed() {
        List<OrderItem> items = List.of(
                OrderItem.of(1L, 1, 270, Money.wons(100000))
        );

        Order order = Order.create(1L, items, Money.wons(100000));

        assertThat(order.getUserId()).isEqualTo(1L);
        assertThat(order.getUserId()).isEqualTo(1L);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualTo(100000);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("생성된 주문은 취소할 수 있다")
    void cancel_order_should_change_status_to_cancelled() {
        Order order = Order.create(1L,
                List.of(OrderItem.of(1L, 1, 270, Money.wons(100000))),
                Money.wons(100000));

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("이미 취소된 주문은 다시 취소할 수 없다")
    void cancel_order_should_fail_if_not_created_status() {
        Order order = Order.create(1L,
                List.of(OrderItem.of(1L, 1, 270, Money.wons(100000))),
                Money.wons(100000));

        order.cancel(); // 상태를 CANCELLED로 전환

        assertThrows(OrderException.InvalidStateException.class, order::cancel);
    }

    @Test
    @DisplayName("생성된 주문은 CONFIRMED 상태로 변경할 수 있다")
    void mark_order_as_confirmed_should_change_status_to_confirmed() {
        Order order = Order.create(1L,
                List.of(OrderItem.of(1L, 1, 270, Money.wons(100000))),
                Money.wons(100000));

        order.markConfirmed();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("이미 CONFIRMED 상태인 주문은 다시 CONFIRMED로 변경할 수 없다")
    void mark_confirmed_should_fail_if_not_created_status() {
        Order order = Order.create(1L,
                List.of(OrderItem.of(1L, 1, 270, Money.wons(100000))),
                Money.wons(100000));

        order.markConfirmed(); // CONFIRMED 상태

        assertThrows(OrderException.InvalidStateException.class, order::markConfirmed);
    }

    @Test
    @DisplayName("주문 아이템이 비어있으면 주문 생성에 실패한다")
    void create_order_should_fail_when_no_items() {
        assertThatThrownBy(() ->
                Order.create(1L, List.of(), Money.wons(0))
        ).isInstanceOf(OrderException.EmptyItemException.class);
    }

    @Test
    @DisplayName("총 주문 금액은 각 아이템의 금액 * 수량의 합과 일치해야 한다")
    void create_order_total_amount_should_match_sum_of_items() {
        List<OrderItem> items = List.of(
                OrderItem.of(1L, 2, 270, Money.wons(50000)), // 2 * 50,000 = 100,000
                OrderItem.of(2L, 1, 270, Money.wons(70000))  // 1 * 70,000 = 70,000
        );

        // 총 합계: 170,000
        Order order = Order.create(1L, items, Money.wons(170000));

        assertThat(order.getTotalAmount()).isEqualTo(170000);
    }

}