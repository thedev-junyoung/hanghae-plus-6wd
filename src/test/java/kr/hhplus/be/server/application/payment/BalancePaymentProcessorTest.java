package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceService;
import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

 
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BalancePaymentProcessorTest {

    private BalanceService balanceService;
    private BalancePaymentProcessor paymentProcessor;

    @BeforeEach
    void setUp() {
        balanceService = mock(BalanceService.class);
        paymentProcessor = new BalancePaymentProcessor(balanceService);
    }

    @Test
    @DisplayName("잔액 결제 프로세서 - 잔액 차감 성공 시 true 반환")
    void process_shouldReturnTrue_whenBalanceIsEnough() {
        // given
        RequestPaymentCommand command = new RequestPaymentCommand("ORDER-001", 100L, 1000L,"BALANCE");

        Order order = Order.create( 100L,
                List.of(OrderItem.of(1L, 2, 270, Money.wons(100000))),
                Money.wons(200000));

        Payment payment = Payment.create(order.getId(), Money.wons(order.getTotalAmount()), PaymentStatus.SUCCESS,"BALANCE");

        when(balanceService.decreaseBalance(any(DecreaseBalanceCommand.class))).thenReturn(true);

        // when
        boolean result = paymentProcessor.process(command, order, payment);

        // then
        assertThat(result).isTrue();

        verify(balanceService).decreaseBalance(eq(
                new DecreaseBalanceCommand(100L, 200000)
        ));

    }
}
