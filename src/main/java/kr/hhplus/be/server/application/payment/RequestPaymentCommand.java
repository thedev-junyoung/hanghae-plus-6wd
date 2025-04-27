package kr.hhplus.be.server.application.payment;


import kr.hhplus.be.server.application.balance.DecreaseBalanceCommand;
import kr.hhplus.be.server.interfaces.payment.PaymentRequest;


public record RequestPaymentCommand(
        String orderId,
        Long userId,
        long amount,  // 결제 금액
        String method  // 예: "BALANCE", "CARD"
) {
    public DecreaseBalanceCommand toDecreaseBalanceCommand(long amount) {
        return new DecreaseBalanceCommand(userId, amount);
    }

}
