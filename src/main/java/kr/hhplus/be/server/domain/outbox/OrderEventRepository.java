package kr.hhplus.be.server.domain.outbox;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderEventRepository {
    void save(OrderEvent event);
    Optional<OrderEvent> findById(UUID id);
    List<OrderEvent> findPendingEvents();
    void markAsSent(UUID id);
}
