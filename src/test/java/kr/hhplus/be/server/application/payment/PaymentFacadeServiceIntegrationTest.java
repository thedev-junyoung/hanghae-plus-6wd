package kr.hhplus.be.server.application.payment;

import jakarta.persistence.EntityManager;
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

    @Autowired
    PaymentFacadeService paymentFacadeService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    BalanceRepository balanceRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    EntityManager entityManager;


    private final Long productId = 1L;
    private final int size = 270;

    @Test
    @DisplayName("성공: 잔액 차감 후 이벤트 기반으로 결제 정보와 주문 상태가 정확히 변경된다")
    void should_process_payment_successfully_and_confirm_order() {
        // given
        Long userId = 1000L;
        long price = 199000L;

        // ⭐ 반드시 충분한 잔액으로 초기화
        balanceRepository.findByUserId(userId).ifPresentOrElse(
                balance -> {
                    balance.charge(Money.wons(1_000_000L)); // 충분히 충전
                    balanceRepository.save(balance);
                },
                () -> balanceRepository.save(Balance.createNew(userId, Money.wons(1_000_000L)))
        );

        long originalBalance = balanceRepository.findByUserId(userId)
                .orElseThrow().getAmount();

        // 주문 생성
        Order order = Order.create(userId,
                List.of(OrderItem.of(productId, 1, size, Money.wons(price))),
                Money.wons(price));
        orderRepository.save(order);

        RequestPaymentCommand command = new RequestPaymentCommand(order.getId(), userId, price, "BALANCE");

        // when
        PaymentResult result = paymentFacadeService.requestPayment(command);

        // then - 잔액은 즉시 검증 가능
        Balance updatedBalance = balanceRepository.findByUserId(userId).orElseThrow();
        assertThat(updatedBalance.getAmount()).isEqualTo(originalBalance - price);

        // 비동기 이벤트 결과 대기 → 결제 정보 & 주문 상태
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new AssertionError("결제 정보가 아직 생성되지 않았습니다."));
            assertThat(payment.getAmount()).isEqualTo(price);
            assertThat(payment.getMethod()).isEqualTo("BALANCE");

            entityManager.clear();
            Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        });

        assertThat(result.status()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("잔액 부족 시 예외 발생 및 상태 불변")
    void requestPayment_fail_ifInsufficientBalance() {
        Long userId = 101L;
        long tooMuch = 500_000L;

        balanceRepository.findByUserId(userId).ifPresentOrElse(
                balance -> {
                    balance.decrease(Money.wons(balance.getAmount()));
                    balanceRepository.save(balance);
                },
                () -> balanceRepository.save(Balance.createNew(userId, Money.wons(0L)))
        );

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