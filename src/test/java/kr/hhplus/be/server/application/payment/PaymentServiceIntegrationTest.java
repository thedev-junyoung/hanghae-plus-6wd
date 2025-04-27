package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("결제 성공 정보를 저장하고 실제 DB에 저장된다 (order-1 사용)")
    void recordSuccess_shouldPersistToDatabase_usingExistingOrder() {
        // given
        String orderId = "order-1"; // 실제 DB에 존재하는 주문 ID
        long amount = 398000L;
        String method = "CARD";

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
