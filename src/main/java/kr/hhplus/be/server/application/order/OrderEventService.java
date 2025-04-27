package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.outbox.OrderEvent;
import kr.hhplus.be.server.domain.outbox.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventService implements OrderEventUseCase {

    private final OrderEventRepository repository;

    public void recordPaymentCompletedEvent(Order order) {
        OrderEvent event = OrderEvent.paymentCompleted(order);  // 📌 도메인 객체 사용
        repository.save(event);
    }
}
