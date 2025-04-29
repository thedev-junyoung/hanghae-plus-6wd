package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.rate.InMemoryRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class BalanceFacade {

    private final BalanceRetryService retryService;
    private final BalanceHistoryUseCase historyUseCase;
    private final InMemoryRateLimiter rateLimiter;



    @Transactional
    @DistributedLock(
            key = "'balance:charge:' + #criteria.userId()",
            waitTime = 10,        // 최대 10초까지 락 기다린다
            leaseTime = 3,        // 락 유지시간은 3초만
            timeUnit = TimeUnit.SECONDS
    )
    public BalanceResult charge(ChargeBalanceCriteria criteria) {
        rateLimiter.validate(criteria.userId());

        ChargeBalanceCommand command = ChargeBalanceCommand.from(criteria);
        BalanceInfo info = retryService.chargeWithRetry(command);
        historyUseCase.recordHistory(RecordBalanceHistoryCommand.of(criteria));
        return BalanceResult.fromInfo(info);
    }


}

