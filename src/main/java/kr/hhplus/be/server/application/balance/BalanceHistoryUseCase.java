package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.Balance;

import java.util.Optional;

public interface BalanceHistoryUseCase {
    /**
     * 사용자 잔액 이력을 기록합니다. -> 이벤트 발행으로 대체
     */
    void recordHistory(RecordBalanceHistoryCommand command);

    /**
     * 멱등 요청 여부와 함께 기존 잔액 반환
     */
    Optional<Balance> findIfDuplicatedRequest(String requestId, Long userId);


}
