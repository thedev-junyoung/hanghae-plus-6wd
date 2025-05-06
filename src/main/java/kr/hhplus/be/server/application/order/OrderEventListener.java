package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderException;
import kr.hhplus.be.server.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderRepository orderRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("[OrderEventListener] AFTER_COMMIT: 결제 완료 이벤트 수신 - {}", event);

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderException.NotFoundException(event.orderId()));

        order.markConfirmed();
        orderRepository.save(order);

        log.info("[OrderEventListener] 주문 상태 변경 완료: orderId={}, status={}", order.getId(), order.getStatus());
    }
}
