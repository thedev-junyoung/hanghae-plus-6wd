package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.order.PaymentCompletedEvent;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private ApplicationEventPublisher eventPublisher;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        paymentService = new PaymentService(paymentRepository, eventPublisher);
    }

    @Test
    @DisplayName("결제 성공 정보를 저장하고 반환한다")
    void recordSuccess_shouldSavePaymentWithSuccessStatus() {
        // given
        PaymentCommand command = new PaymentCommand("order-123", Money.wons(10000), "BALANCE");


        // when
        Payment payment = paymentService.recordSuccess(command);


        // then
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));

        assertThat(payment).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo("order-123");
        assertThat(payment.getAmount()).isEqualTo(10000L);
        assertThat(payment.getMethod()).isEqualTo("BALANCE");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }


}