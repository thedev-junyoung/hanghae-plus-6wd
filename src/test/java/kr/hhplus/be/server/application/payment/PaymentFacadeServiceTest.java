package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceService;
import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.application.order.PaymentCompletedEvent;
import kr.hhplus.be.server.common.lock.AopForTransaction;
import kr.hhplus.be.server.common.lock.DistributedLockExecutor;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentFacadeServiceTest {

    private BalanceService balanceService;
    private PaymentFacadeService facadeService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        balanceService = mock(BalanceService.class);
        paymentService = mock(PaymentService.class);
        facadeService = new PaymentFacadeService(balanceService, paymentService);
    }

    @Test
    @DisplayName("잔액 차감 → 결제 성공 처리")
    void requestPayment_success() {
        // Given
        String orderId = "ORDER-123";
        long userId = 1L;
        long amount = 10000L;
        String method = "BALANCE";

        RequestPaymentCommand command = new RequestPaymentCommand(orderId, userId, amount, method);
        Money expectedMoney = Money.wons(amount);
        Payment mockPayment = Payment.createSuccess(orderId, expectedMoney, method);


        // When
        when(paymentService.recordSuccess(any(PaymentCommand.class))).thenReturn(mockPayment);



        PaymentResult result = facadeService.requestPayment(command);

        // Then
        verify(balanceService).decreaseBalance(new DecreaseBalanceCommand(userId, amount));
        verify(paymentService).recordSuccess(new PaymentCommand(orderId, Money.from(amount), method));

        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.amount()).isEqualTo(amount);
        assertThat(result.status()).isEqualTo("SUCCESS");
    }
}
