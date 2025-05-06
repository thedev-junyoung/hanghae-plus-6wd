package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceHistoryEventHandler {

    private final BalanceHistoryRepository balanceHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    @Async
    public void handle(BalanceChargedEvent event) {
        log.info("[BalanceHistoryEventHandler] AFTER_COMMIT: 잔액 충전 이력 저장 - {}", event);

        if (balanceHistoryRepository.existsByRequestId(event.requestId())) {
            log.warn("[BalanceHistoryEventHandler] 중복된 이력 무시: requestId={}", event.requestId());
            return;
        }

        BalanceHistory history = BalanceHistory.charge(
                event.userId(), event.amount(), event.reason(), event.requestId()
        );
        balanceHistoryRepository.save(history);
    }
}

