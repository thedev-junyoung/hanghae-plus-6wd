package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceUseCase;
import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * BalancePaymentProcessor는  결제 수단에 대한 처리만 한다 (잔액 차감만).
 */
@Component
@RequiredArgsConstructor
public class BalancePaymentProcessor implements PaymentProcessor {

    private final BalanceUseCase balanceUseCase;

    @Override
    public boolean process(RequestPaymentCommand command, Order order, Payment payment) {
        DecreaseBalanceCommand decreaseCommand = command.toDecreaseBalanceCommand(order.getTotalAmount());
        return balanceUseCase.decreaseBalance(decreaseCommand);
    }
}
