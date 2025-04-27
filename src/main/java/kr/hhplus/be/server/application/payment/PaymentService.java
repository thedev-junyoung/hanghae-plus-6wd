package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentUseCase {

    private final PaymentRepository paymentRepository;

    public Payment recordSuccess(PaymentCommand command) {
        Payment payment = Payment.createSuccess(command.orderId(), command.amount(), command.method());
        paymentRepository.save(payment);
        return payment;
    }


}
