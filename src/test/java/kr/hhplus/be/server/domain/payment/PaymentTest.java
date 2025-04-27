package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @Test
    @DisplayName("성공 상태의 결제 객체 생성 테스트")
    void create_success_payment() {
        // given
        String orderId = "order-123";
        Money amount = Money.wons(10000);
        String method = "BALANCE";

        // when
        Payment payment = Payment.createSuccess(orderId, amount, method);

        // then
        assertThat(payment).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualTo(amount.value());
        assertThat(payment.getMethod()).isEqualTo(method);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("실패 상태의 결제 객체 생성 테스트")
    void create_failure_payment() {
        // given
        String orderId = "order-456";
        Money amount = Money.wons(5000);
        String method = "BALANCE";

        // when
        Payment payment = Payment.createFailure(orderId, amount, method);

        // then
        assertThat(payment).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualTo(amount.value());
        assertThat(payment.getMethod()).isEqualTo(method);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILURE);
        assertThat(payment.isSuccess()).isFalse();
    }
}
