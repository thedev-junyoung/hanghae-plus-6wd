package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.balance.BalanceUseCase;
import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentFacadeService {

    private final BalanceUseCase balanceUseCase;
    private final PaymentUseCase paymentUseCase;


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

        return PaymentResult.from(payment);

    }

}
