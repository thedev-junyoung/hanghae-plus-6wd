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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class PaymentFacadeServiceIntegrationTest {

    @Autowired
    PaymentFacadeService paymentFacadeService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    BalanceRepository balanceRepository;

    @Autowired
    OrderRepository orderRepository;

    private final Long productId = 1L;
    private final int size = 270;

    @Test
    @DisplayName("잔액 차감 → 결제 기록 → 주문 상태 변경까지 전체 흐름을 검증한다")
    void requestPayment_shouldDeductBalance_RecordPayment_AndConfirmOrder() {
        // given
        // 실제 데이터 기준
        Long userId = 100L;
        long originalBalance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("balance not found"))
                .getAmount();

        long productPrice = 199000L;
        Order order = Order.create(
                userId,
                List.of(OrderItem.of(productId, 1, size, Money.wons(productPrice))),
                Money.wons(productPrice)
        );
        orderRepository.save(order);

        RequestPaymentCommand command = new RequestPaymentCommand(order.getId(), userId, productPrice, "BALANCE");

        // when
        PaymentResult result = paymentFacadeService.requestPayment(command);

        // then
        Balance updated = balanceRepository.findByUserId(userId).orElseThrow();
        assertThat(updated.getAmount()).isEqualTo(originalBalance - productPrice);

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();
        assertThat(payment.getAmount()).isEqualTo(productPrice);
        assertThat(payment.getMethod()).isEqualTo("BALANCE");

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        assertThat(result.orderId()).isEqualTo(order.getId());
        assertThat(result.status()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("잔액 부족 시 결제 요청은 실패해야 한다")
    void requestPayment_fail_ifNotEnoughBalance() {
        // given
        Long lowBalanceUserId = 101L; // User 101의 잔액 300,000
        long paymentAmount = 500_000L; // 주문 금액이 잔액보다 큼

        Order order = Order.create(
                lowBalanceUserId,
                List.of(OrderItem.of(productId, 1, size, Money.wons(paymentAmount))),
                Money.wons(paymentAmount)
        );
        orderRepository.save(order);

        RequestPaymentCommand command = new RequestPaymentCommand(order.getId(), lowBalanceUserId, paymentAmount, "BALANCE");

        // when & then
        assertThatThrownBy(() -> paymentFacadeService.requestPayment(command))
                .isInstanceOf(RuntimeException.class) // 실제 예외 클래스로 교체 가능
                .hasMessageContaining("잔액이 부족");

        // 상태 불변 확인
        Balance balance = balanceRepository.findByUserId(lowBalanceUserId).orElseThrow();
        assertThat(balance.getAmount()).isEqualTo(300000L);

        assertThat(paymentRepository.findByOrderId(order.getId())).isEmpty();

        Order unchanged = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.CREATED);
    }
}

