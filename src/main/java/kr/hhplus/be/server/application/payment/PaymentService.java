package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentUseCase {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment recordSuccess(PaymentCommand command) {
        Payment payment = Payment.createSuccess(command.orderId(), command.amount(), command.method());
        paymentRepository.save(payment);
        return payment;
    }


}