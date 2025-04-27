package kr.hhplus.be.server.domain.balance;

import java.util.Optional;

public interface BalanceRepository{
    /**
     * 사용자 잔액을 저장하거나 업데이트한다.
     */
    Balance save(Balance balance);

    /**
     * 사용자 잔액을 조회한다.
     */
    Optional<Balance> findByUserId(Long userId);

    void flush();

    void clear();
}