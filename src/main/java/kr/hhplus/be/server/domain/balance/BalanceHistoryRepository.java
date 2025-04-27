package kr.hhplus.be.server.domain.balance;


import java.util.List;

public interface BalanceHistoryRepository {
    void save(BalanceHistory history);
    List<BalanceHistory> findAllByUserId(long userId);

    boolean existsByUserIdAndReason(Long userId, String reason);

    boolean existsByRequestId(String s);
}
