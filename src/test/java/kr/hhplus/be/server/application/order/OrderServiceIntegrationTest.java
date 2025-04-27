package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;


@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderService orderService;

    @Test
    @DisplayName("주문을 생성하고 DB에 저장되며, 연관된 주문 상품도 저장된다")
    void createOrder_success() {
        // given
        Long userId = 100L;               // 실제 DB에 존재하는 사용자
        Long productId = 1L;              // 실제 존재하는 상품 (New Balance 993)
        int quantity = 2;
        int size = 270;
        long unitPrice = 199000L;

        List<OrderItem> items = List.of(OrderItem.of(productId, quantity, size, Money.wons(unitPrice)));
        Money total = Money.wons(unitPrice * quantity);

        // when
        Order order = orderService.createOrder(userId, items, total);

        // then
        Order saved = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(Money.from(saved.getTotalAmount())).isEqualTo(total);
        assertThat(saved.getItems()).hasSize(1);

        OrderItem savedItem = saved.getItems().get(0);
        assertThat(savedItem.getProductId()).isEqualTo(productId);
        assertThat(savedItem.getQuantity()).isEqualTo(quantity);
        assertThat(savedItem.getSize()).isEqualTo(size);
        assertThat(savedItem.calculateTotal().value()).isEqualTo(unitPrice * quantity);
    }


    @Test
    @DisplayName("주문 상태를 CONFIRMED로 변경할 수 있다")
    void markConfirmed_success() {
        Long userId = 100L;
        Long productId = 2L; // ASICS GEL-Kayano 14
        int size = 275;
        long unitPrice = 169000L;

        List<OrderItem> items = List.of(OrderItem.of(productId, 1, size, Money.wons(unitPrice)));
        Money total = Money.wons(unitPrice);

        Order order = orderService.createOrder(userId, items, total);
        orderService.markConfirmed(order);

        Order confirmed = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

}

