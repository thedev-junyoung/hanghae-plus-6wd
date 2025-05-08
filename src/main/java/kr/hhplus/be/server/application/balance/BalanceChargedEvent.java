package kr.hhplus.be.server.application.balance;

public record BalanceChargedEvent(
        Long userId,
        long amount,
        String reason,
        String requestId
) {
    public static BalanceChargedEvent from(ChargeBalanceCommand command) {
        return new BalanceChargedEvent(
                command.userId(),
                command.amount(),
                command.reason(),
                command.requestId()
        );
    }
}

