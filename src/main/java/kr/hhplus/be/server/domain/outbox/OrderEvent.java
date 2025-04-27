package kr.hhplus.be.server.domain.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.orderexport.OrderExportPayload;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEvent {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;  // ex: "ORDER"

    @Column(nullable = false)
    private String eventType;      // ex: "PAYMENT_COMPLETED"


    @Column(name = "payload", columnDefinition = "LONGTEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static OrderEvent paymentCompleted(Order order) {
        OrderExportPayload exportPayload = OrderExportPayload.from(order);
        return new OrderEvent(
                UUID.randomUUID(),
                "ORDER",
                "PAYMENT_COMPLETED",
                toJson(exportPayload),
                EventStatus.PENDING,
                LocalDateTime.now()
        );
    }

    private OrderEvent(UUID id, String aggregateType, String eventType, String payload, EventStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
    }

    private static String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Payload 직렬화 실패", e);
        }
    }

    public void markAsProcessedSuccessfully() {
        this.status = EventStatus.COMPLETED;
    }
}
