package kr.hhplus.be.server.infrastructure.balance;

import kr.hhplus.be.server.domain.balance.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceHistoryJpaRepository extends JpaRepository<BalanceHistory, Long> {
    List<BalanceHistory> findAllByUserId(long userId);

    boolean existsByUserIdAndReason(Long userId, String reason);

    boolean existsByRequestId(String s);
}
