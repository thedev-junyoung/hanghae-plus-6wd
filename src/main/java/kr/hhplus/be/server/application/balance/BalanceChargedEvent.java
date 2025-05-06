package kr.hhplus.be.server.application.balance;

public record BalanceChargedEvent(
        Long userId,
        long amount,
        String reason,
        String requestId
) {
    public static BalanceChargedEvent from(ChargeBalanceCriteria criteria) {
        return new BalanceChargedEvent(
                criteria.userId(),
                criteria.amount(),
                criteria.reason(),
                criteria.requestId()
        );
    }
}

