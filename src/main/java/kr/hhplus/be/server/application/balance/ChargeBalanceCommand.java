package kr.hhplus.be.server.application.balance;


public record ChargeBalanceCommand(
        Long userId,
        long amount,
        String reason,
        String requestId
) {
    public static ChargeBalanceCommand from(ChargeBalanceCriteria criteria) {
        return new ChargeBalanceCommand(
                criteria.userId(),
                criteria.amount(),
                criteria.reason(),
                criteria.requestId()
        );
    }
}
