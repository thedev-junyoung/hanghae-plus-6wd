package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceUseCase;
import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.application.order.PaymentCompletedEvent;
import kr.hhplus.be.server.common.lock.DistributedLock;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFacadeService {
    private final BalanceUseCase balanceUseCase;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @DistributedLock(key = "#command.orderId", prefix = "payment:order:")
    public PaymentResult requestPayment(RequestPaymentCommand command) {
        balanceUseCase.decreaseBalance(
                DecreaseBalanceCommand.of(command.userId(), command.amount())
        );

        eventPublisher.publishEvent(new PaymentSuccessEvent(command));
        eventPublisher.publishEvent(new PaymentCompletedEvent(command.orderId()));

        // 응답은 DB에 저장된 ID가 없어도 바로 반환
        return PaymentResult.success(command.orderId(), command.amount(), command.method());
    }

}
