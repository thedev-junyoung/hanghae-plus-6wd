package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceUseCase;
import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.application.order.OrderUseCase;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFacadeService {
    private final PaymentUseCase paymentUseCase;
    private final OrderUseCase orderUseCase;
    private final BalanceUseCase balanceUseCase;

    // PaymentFacadeService.java
    @Transactional
    public PaymentResult requestPayment(RequestPaymentCommand command) {
        Money amount = Money.wons(command.amount());

        // 1. 주문을 가져온다.(주문이 존재하는지, 결제 가능한 상태인지 검증)
//        Order order = orderUseCase.getOrderForPayment(command.orderId());
        Order order = orderUseCase.getOrderForPaymentWithLock(command.orderId());

        // 2. 잔액 차감
        balanceUseCase.decreaseBalance(
                new DecreaseBalanceCommand(command.userId(), amount.value())
        );

        // 3. 결제 성공 처리
        Payment payment = paymentUseCase.recordSuccess(
                PaymentCommand.from(command)
        );

        // 4. 주문 상태를 결제 완료로 변경
        orderUseCase.markConfirmed(order);

        return PaymentResult.from(payment);
    }

}
