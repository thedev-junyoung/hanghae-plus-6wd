package kr.hhplus.be.server.domain.order;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private String orderId; // 또는 @ManyToOne(Order order)

    @Column(nullable = false)
    private String status;

    @Column(name = "memo", columnDefinition = "LONGTEXT", nullable = false)
    private String memo;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;


    public OrderHistory(String orderId, String status, String memo) {
        this.orderId = orderId;
        this.status = status;
        this.memo = memo;
        this.changedAt = LocalDateTime.now();
    }

    public static OrderHistory create(String orderId, String status, String memo) {
        return new OrderHistory(orderId, status, memo);
    }
    public boolean isStatusChangeTo(OrderStatus targetStatus) {
        return OrderStatus.valueOf(this.status) == targetStatus;
    }

    public boolean isCancelled() {
        return isStatusChangeTo(OrderStatus.CANCELLED);
    }



}
