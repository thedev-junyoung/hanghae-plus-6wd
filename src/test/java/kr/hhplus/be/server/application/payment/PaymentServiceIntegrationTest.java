package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("결제 성공 정보를 저장하고 실제 DB에 저장된다 (order-1 사용)")
    void recordSuccess_shouldPersistToDatabase_usingExistingOrder() {
        // given
        Long userId = 1000L;
        long amount = 398000L;
        String method = "CARD";
        // 새로운 주문 생성
        Order order = Order.create(userId,
                List.of(OrderItem.of(1L, 1, 270, Money.wons(amount))),
                Money.wons(amount));
        orderRepository.save(order);

        String orderId = order.getId();

        PaymentCommand command = new PaymentCommand(orderId, Money.wons(amount), method);

        // when
        Payment saved = paymentService.recordSuccess(command);

        // then
        Payment found = paymentRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getOrderId()).isEqualTo(orderId);
        assertThat(found.getAmount()).isEqualTo(amount);
        assertThat(found.getMethod()).isEqualTo(method);
        assertThat(found.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(found.getCreatedAt()).isNotNull();
    }
}