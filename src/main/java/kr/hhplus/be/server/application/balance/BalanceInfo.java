package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.Balance;

import java.time.LocalDateTime;

public record BalanceInfo(
        Long userId,
        Long amount,
        LocalDateTime updatedAt
) {
    public static BalanceInfo from(Balance balance) {
        return new BalanceInfo(
                balance.getUserId(),
                balance.getAmount(),
                balance.getUpdatedAt()
        );
    }
}
