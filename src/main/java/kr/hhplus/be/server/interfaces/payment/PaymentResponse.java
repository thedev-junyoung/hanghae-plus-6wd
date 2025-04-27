package kr.hhplus.be.server.interfaces.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.application.payment.PaymentResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentResponse {

    @Schema(description = "결제 ID", example = "pay_123456")
    private final String paymentId;

    @Schema(description = "주문 ID", example = "order_78910")
    private final String orderId;

    @Schema(description = "결제 금액", example = "10000")
    private final long amount;

    @Schema(description = "결제 수단", example = "BALANCE")
    private final String method;

    @Schema(description = "결제 상태", example = "SUCCESS")
    private final String status;

    @Schema(description = "결제 생성 시간", example = "2025-04-10T14:30:00")
    private final LocalDateTime createdAt;

    public static PaymentResponse from(PaymentResult result) {
        return new PaymentResponse(
                result.paymentId(),
                result.orderId(),
                result.amount(),
                result.method(),
                result.status(),
                result.createdAt()
        );
    }
}
