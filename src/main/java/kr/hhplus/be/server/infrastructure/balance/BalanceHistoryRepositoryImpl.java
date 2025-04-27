package kr.hhplus.be.server.infrastructure.balance;

import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BalanceHistoryRepositoryImpl implements BalanceHistoryRepository {

    private final BalanceHistoryJpaRepository balanceHistoryJpaRepository;

    @Override
    public void save(BalanceHistory history) {
        balanceHistoryJpaRepository.save(history);
    }

    @Override
    public List<BalanceHistory> findAllByUserId(long userId) {
        return balanceHistoryJpaRepository.findAllByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndReason(Long userId, String reason) {
        return balanceHistoryJpaRepository.existsByUserIdAndReason(userId, reason);
    }

    @Override
    public boolean existsByRequestId(String s) {
        return balanceHistoryJpaRepository.existsByRequestId(s);
    }


}
