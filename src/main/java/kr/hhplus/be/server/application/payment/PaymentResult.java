package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.domain.payment.Payment;

import java.time.LocalDateTime;
public record PaymentResult(
        String paymentId,
        String orderId,
        long amount,
        String method,
        String status,
        LocalDateTime createdAt
) {
    public static PaymentResult from(Payment payment) {
        return new PaymentResult(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );
    }
    public static PaymentResult from(PaymentInfo info) {
        return new PaymentResult(
                info.paymentId(),
                info.orderId(),
                info.amount(),
                info.method(),
                info.status(),
                info.createdAt()
        );
    }
}
