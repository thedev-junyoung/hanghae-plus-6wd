package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceUseCase;
import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.application.order.PaymentCompletedEvent;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.domain.payment.Payment;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFacadeService {

    private final BalanceUseCase balanceUseCase;
    private final PaymentUseCase paymentUseCase;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    @DistributedLock(key = "#command.orderId", prefix = "payment:order:")
    public PaymentResult requestPayment(RequestPaymentCommand command) {
        /*
          1. 잔랙 차감 처리
         */
        balanceUseCase.decreaseBalance(
                DecreaseBalanceCommand.of(command.userId(), command.amount())
        );

        /*
          2. 결제 성공 처리
         */
        Payment payment = paymentUseCase.recordSuccess(
                PaymentCommand.from(command)
        );

        /*
          3. OrderStatus.CONFIRMED 상태로 변경하기 위한 이벤트 발행
         */
        eventPublisher.publishEvent(new PaymentCompletedEvent(command.orderId()));

        return PaymentResult.from(payment);

    }

}
