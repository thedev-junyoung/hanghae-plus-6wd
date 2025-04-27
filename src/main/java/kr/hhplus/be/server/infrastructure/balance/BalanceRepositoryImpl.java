package kr.hhplus.be.server.infrastructure.balance;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BalanceRepositoryImpl implements BalanceRepository {

    private final BalanceJpaRepository jpaRepository;
    private final EntityManager entityManager;

    @Override
    public Balance save(Balance balance) {
        return jpaRepository.save(balance);
    }

    @Override
    public Optional<Balance> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    public void flush() {
        entityManager.flush();
    }

    @Override
    public void clear() {
        entityManager.clear();
    }
}
