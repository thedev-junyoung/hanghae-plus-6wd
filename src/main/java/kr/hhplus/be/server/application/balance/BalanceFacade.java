package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.rate.InMemoryRateLimiter;
import kr.hhplus.be.server.domain.balance.Balance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceFacade {

    private final BalanceRetryService retryService;
    private final BalanceHistoryUseCase historyUseCase;
    private final InMemoryRateLimiter rateLimiter;


    @DistributedLock(key = "'balance:charge:' + #criteria.userId")
    public BalanceResult charge(ChargeBalanceCriteria criteria) {
        rateLimiter.validate(criteria.userId());

        Optional<Balance> duplicated = historyUseCase.findIfDuplicatedRequest(criteria.requestId(), criteria.userId());
        if (duplicated.isPresent()) {
            log.warn("[멱등 요청] 이미 처리된 충전: requestId={}, userId={}", criteria.requestId(), criteria.userId());
            return BalanceResult.fromInfo(BalanceInfo.from(duplicated.get()));
        }
        BalanceInfo info = retryService.chargeWithRetry(ChargeBalanceCommand.from(criteria));
        return BalanceResult.fromInfo(info);
    }

}

