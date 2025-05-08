package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderException;
import kr.hhplus.be.server.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderRepository orderRepository;


    /**
     * 현재는 테스트 용이성을 위해 BEFORE_COMMIT으로 동기 처리.
     * TODO: 추후 AFTER_COMMIT + @Async로 변경하고 실패 시 보상 트랜잭션 구현 필요
     * - 메시지 큐 도입 고려
     * - 실패 시 재시도 로직 추가
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("[OrderEventListener] AFTER_COMMIT: 결제 완료 이벤트 수신 - {}", event);

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderException.NotFoundException(event.orderId()));

        order.markConfirmed();
        orderRepository.save(order);

        log.info("[OrderEventListener] 주문 상태 변경 완료: orderId={}, status={}", order.getId(), order.getStatus());
    }
}
