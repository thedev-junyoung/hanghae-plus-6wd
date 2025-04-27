package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.rate.InMemoryRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class BalanceFacade {

    private final BalanceRetryService retryService;
    private final BalanceHistoryUseCase historyUseCase;
    private final InMemoryRateLimiter rateLimiter;



    @Transactional
    public BalanceResult charge(ChargeBalanceCriteria criteria) {
        rateLimiter.validate(criteria.userId());

        ChargeBalanceCommand command = ChargeBalanceCommand.from(criteria);
        BalanceInfo info = retryService.chargeWithRetry(command);
        historyUseCase.recordHistory(RecordBalanceHistoryCommand.of(criteria));
        return BalanceResult.fromInfo(info);
    }


}

