package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceService;
import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.application.order.OrderService;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentFacadeServiceTest {

    private PaymentService paymentService;
    private OrderService orderService;
    private BalanceService balanceService;

    private PaymentFacadeService facadeService;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        orderService = mock(OrderService.class);
        balanceService = mock(BalanceService.class);

        facadeService = new PaymentFacadeService(paymentService, orderService, balanceService);
    }

    @Test
    @DisplayName("잔액 차감 후 결제 기록 및 주문 상태 변경")
    void requestPayment_success() {
        // Given
        String orderId = "ORDER123";
        long userId = 1L;
        long amount = 10000L;
        String method = "BALANCE";

        RequestPaymentCommand command = new RequestPaymentCommand(orderId, userId, amount, method);
        Money expectedMoney = Money.wons(amount);

        Order order = mock(Order.class);
        when(orderService.getOrderForPaymentWithLock(orderId)).thenReturn(order);

        Payment mockPayment = Payment.createSuccess(orderId, expectedMoney, method);
        when(paymentService.recordSuccess(any(PaymentCommand.class))).thenReturn(mockPayment);

        // When
        PaymentResult result = facadeService.requestPayment(command);

        // Then
        verify(balanceService).decreaseBalance(new DecreaseBalanceCommand(userId, amount));
        verify(orderService).markConfirmed(order);
        verify(paymentService).recordSuccess(PaymentCommand.from(command));

        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.amount()).isEqualTo(amount);
        assertThat(result.status()).isEqualTo("SUCCESS");
    }
}
