package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.domain.payment.Payment;

import java.time.LocalDateTime;

public record PaymentInfo(
        String paymentId,
        String orderId,
        String method,
        String status,
        Long amount,
        LocalDateTime createdAt
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.getId(),
                payment.getOrderId(),
                payment.getMethod(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCreatedAt()
        );
    }
}
