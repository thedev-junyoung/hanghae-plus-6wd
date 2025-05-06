package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceService;
import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.application.order.PaymentCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentFacadeServiceTest {

    private BalanceService balanceService;
    private ApplicationEventPublisher eventPublisher;
    private PaymentFacadeService facadeService;

    @BeforeEach
    void setUp() {
        balanceService = mock(BalanceService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        facadeService = new PaymentFacadeService(balanceService, eventPublisher);
    }

    @Test
    @DisplayName("잔액 차감 후 PaymentSuccessEvent, PaymentCompletedEvent 이벤트 발행")
    void requestPayment_success() {
        // Given
        String orderId = "ORDER-123";
        long userId = 1L;
        long amount = 10000L;
        String method = "BALANCE";

        RequestPaymentCommand command = new RequestPaymentCommand(orderId, userId, amount, method);

        // When
        PaymentResult result = facadeService.requestPayment(command);

        // Then
        verify(balanceService).decreaseBalance(new DecreaseBalanceCommand(userId, amount));
        verify(eventPublisher).publishEvent(new PaymentSuccessEvent(command));
        verify(eventPublisher).publishEvent(new PaymentCompletedEvent(orderId));

        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.amount()).isEqualTo(amount);
        assertThat(result.status()).isEqualTo("SUCCESS");
    }
}
