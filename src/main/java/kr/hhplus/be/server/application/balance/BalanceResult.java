package kr.hhplus.be.server.application.balance;


import java.time.LocalDateTime;

public record BalanceResult(
        Long userId,
        Long balance,
        LocalDateTime updatedAt
) {
    public static BalanceResult fromInfo(BalanceInfo info) {
        return new BalanceResult(
                info.userId(),
                info.amount(),
                info.updatedAt()
        );
    }


}
