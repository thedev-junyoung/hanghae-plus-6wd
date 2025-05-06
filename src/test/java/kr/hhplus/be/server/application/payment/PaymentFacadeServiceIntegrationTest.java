package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class PaymentFacadeServiceIntegrationTest {

    @Autowired PaymentFacadeService paymentFacadeService;
    @Autowired PaymentRepository paymentRepository;
    @Autowired BalanceRepository balanceRepository;
    @Autowired OrderRepository orderRepository;

    private final Long productId = 1L;
    private final int size = 270;

    @Test
    @DisplayName("잔액 차감 → 이벤트 기반 결제 기록 및 주문 상태 변경 검증")
    void requestPayment_success_flow() {
        // given
        Long userId = 100L;
        long originalBalance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("잔액 없음"))
                .getAmount();

        long price = 199000L;
        Order order = Order.create(userId,
                List.of(OrderItem.of(productId, 1, size, Money.wons(price))),
                Money.wons(price));
        orderRepository.save(order);

        RequestPaymentCommand command = new RequestPaymentCommand(order.getId(), userId, price, "BALANCE");

        // when
        PaymentResult result = paymentFacadeService.requestPayment(command);

        // then
        Balance updatedBalance = balanceRepository.findByUserId(userId).orElseThrow();
        assertThat(updatedBalance.getAmount()).isEqualTo(originalBalance - price);

        // await 비동기 이벤트 처리 (payment, order status)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new AssertionError("결제 정보가 없음"));
            assertThat(payment.getAmount()).isEqualTo(price);
            assertThat(payment.getMethod()).isEqualTo("BALANCE");

            Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        });

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.orderId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("잔액 부족 시 예외 발생 및 상태 불변")
    void requestPayment_fail_ifInsufficientBalance() {
        Long userId = 101L;
        long tooMuch = 500_000L;

        Order order = Order.create(userId,
                List.of(OrderItem.of(productId, 1, size, Money.wons(tooMuch))),
                Money.wons(tooMuch));
        orderRepository.save(order);

        RequestPaymentCommand command = new RequestPaymentCommand(order.getId(), userId, tooMuch, "BALANCE");

        assertThatThrownBy(() -> paymentFacadeService.requestPayment(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("잔액이 부족");

        assertThat(paymentRepository.findByOrderId(order.getId())).isEmpty();
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.CREATED);
    }
}
