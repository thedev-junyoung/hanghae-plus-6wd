package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.order.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements PaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Payment recordSuccess(PaymentCommand command) {
        log.info("[비즈니스 로직 시작: 결제 성공 기록] orderId={}, amount={}", command.orderId(), command.amount());

        Payment payment = Payment.createSuccess(command.orderId(), command.amount(), command.method());

        log.info("[DB 저장 직전] orderId={}, paymentMethod={}", command.orderId(), command.method());

        paymentRepository.save(payment);

        log.info("[비즈니스 로직 끝] 결제 완료, paymentId={}", payment.getId());

        eventPublisher.publishEvent(new PaymentCompletedEvent(command.orderId()));
        return payment;
    }

}