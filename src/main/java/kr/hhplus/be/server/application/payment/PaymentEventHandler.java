package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {

    private final PaymentRepository paymentRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("[PaymentEventHandler] 결제 성공 이벤트 수신 - {}", event);
        RequestPaymentCommand command = event.command();
        Payment payment = Payment.createSuccess(
                command.orderId(), Money.from(command.amount()), command.method()
        );
        paymentRepository.save(payment);
    }


    @EventListener
    public void onDefaultEvent(PaymentSuccessEvent event) {
        log.info("[@EventListener] 기본 동작 - {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onBeforeCommit(PaymentSuccessEvent event) {
        log.info("[@TransactionalEventListener] BEFORE_COMMIT - {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAfterCommit(PaymentSuccessEvent event) {
        log.info("[@TransactionalEventListener] AFTER_COMMIT - {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onAfterRollback(PaymentSuccessEvent event) {
        log.info("[@TransactionalEventListener] AFTER_ROLLBACK - {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void onAfterCompletion(PaymentSuccessEvent event) {
        log.info("[@TransactionalEventListener] AFTER_COMPLETION - {}", event);
    }
}

