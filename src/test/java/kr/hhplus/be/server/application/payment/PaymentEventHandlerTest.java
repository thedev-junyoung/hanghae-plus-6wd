package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PaymentEventHandlerTest {

    private PaymentRepository paymentRepository;
    private PaymentEventHandler eventHandler;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        eventHandler = new PaymentEventHandler(paymentRepository);
    }

    @Test
    @DisplayName("결제 성공 이벤트 발생 시 Payment 저장")
    void handlePaymentSuccess_shouldSavePayment() {
        // Given
        RequestPaymentCommand command = new RequestPaymentCommand("ORDER-001", 1L, 5000L, "BALANCE");
        PaymentSuccessEvent event = new PaymentSuccessEvent(command);

        // When
        eventHandler.handlePaymentSuccess(event);

        // Then
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}
