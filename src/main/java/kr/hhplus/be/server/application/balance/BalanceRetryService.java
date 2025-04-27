package kr.hhplus.be.server.application.balance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceRetryService {

    private final BalanceUseCase balanceService;


    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 100)
    )
    public BalanceInfo chargeWithRetry(ChargeBalanceCommand command) {
        try {
            return balanceService.charge(command);
        } catch (OptimisticLockingFailureException e) {
            log.warn("[재시도 발생] 충전 중 충돌: {}", e.getMessage());
            throw e;
        }
    }
}