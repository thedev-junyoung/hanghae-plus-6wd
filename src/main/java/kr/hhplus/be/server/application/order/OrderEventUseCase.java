package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.Order;

public interface OrderEventUseCase {
    void recordPaymentCompletedEvent(Order order);

}
