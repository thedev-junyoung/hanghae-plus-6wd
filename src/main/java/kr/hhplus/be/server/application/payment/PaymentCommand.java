package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.common.vo.Money;

public record PaymentCommand(
        String orderId,
        Money amount,
        String method
) {
    public static PaymentCommand from(RequestPaymentCommand request) {
        return new PaymentCommand(
                request.orderId(),
                Money.wons(request.amount()),
                request.method()
        );
    }
}
