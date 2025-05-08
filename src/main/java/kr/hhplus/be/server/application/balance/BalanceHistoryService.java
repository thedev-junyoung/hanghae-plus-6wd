package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceHistoryService implements BalanceHistoryUseCase {

    private final BalanceHistoryRepository balanceHistoryRepository;
    private final BalanceRepository balanceRepository;

    public void recordHistory(RecordBalanceHistoryCommand command) {
        if (balanceHistoryRepository.existsByRequestId(command.requestId())) {
            log.warn("중복된 충전 요청 무시됨: {}", command.requestId());
            return;
        }

        BalanceHistory history = BalanceHistory.charge(
                command.userId(), command.amount(), command.reason(), command.requestId()
        );
        balanceHistoryRepository.save(history);
    }

    @Override
    public Optional<Balance> findIfDuplicatedRequest(String requestId, Long userId) {
        if (!balanceHistoryRepository.existsByRequestId(requestId)) {
            return Optional.empty();
        }

        return balanceRepository.findByUserId(userId);
    }

}
