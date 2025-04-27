package kr.hhplus.be.server.domain.payment;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.vo.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Payment(String id, String orderId, Money amount, PaymentStatus status, String method, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount.value();
        this.status = status;
        this.method = method;
        this.createdAt = createdAt;
    }

    public static Payment create(String orderId, Money amount, PaymentStatus status, String method) {
        return new Payment(
                UUID.randomUUID().toString(),
                orderId,
                amount,
                status,
                method,
                LocalDateTime.now()
        );
    }

    public static Payment createSuccess(String orderId, Money amount, String method) {
        return new Payment(
                UUID.randomUUID().toString(),
                orderId,
                amount,
                PaymentStatus.SUCCESS,
                method,
                LocalDateTime.now()
        );
    }

    public static Payment createFailure(String orderId, Money amount, String method) {
        return new Payment(
                UUID.randomUUID().toString(),
                orderId,
                amount,
                PaymentStatus.FAILURE,
                method,
                LocalDateTime.now()
        );
    }

    public boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }
}

