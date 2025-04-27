package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.interfaces.balance.BalanceRequest;

public record ChargeBalanceCriteria(
        Long userId,
        long amount,
        String reason,
        String requestId // ✅ 고유 요청 식별자
) {
    public static ChargeBalanceCriteria of(Long userId, long amount, String reason, String requestId) {
        return new ChargeBalanceCriteria(userId, amount, reason, requestId);
    }

    public static ChargeBalanceCriteria fromRequest(BalanceRequest request) {
        return new ChargeBalanceCriteria(
                request.userId(),
                request.amount(),
                "사용자 요청에 따른 충전",
                request.requestId() // ✅ 프론트에서 넘겨주는 requestId
        );
    }
}
