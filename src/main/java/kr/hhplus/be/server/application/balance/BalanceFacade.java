package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.lock.AopForTransaction;
import kr.hhplus.be.server.common.lock.DistributedLockExecutor;
import kr.hhplus.be.server.common.rate.InMemoryRateLimiter;
import kr.hhplus.be.server.domain.balance.Balance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceFacade {

    private final BalanceRetryService retryService;
    private final BalanceHistoryUseCase historyUseCase;
    private final InMemoryRateLimiter rateLimiter;
    private final ApplicationEventPublisher eventPublisher;
    private final DistributedLockExecutor lockExecutor;
    private final AopForTransaction aopForTransaction;


    public BalanceResult charge(ChargeBalanceCriteria criteria) {
        rateLimiter.validate(criteria.userId());

        Optional<Balance> duplicated = historyUseCase.findIfDuplicatedRequest(criteria.requestId(), criteria.userId());
        if (duplicated.isPresent()) {
            log.warn("[멱등 요청] 이미 처리된 충전: requestId={}, userId={}", criteria.requestId(), criteria.userId());
            return BalanceResult.fromInfo(BalanceInfo.from(duplicated.get()));
        }

        // 이 부분만 분산락
        return lockExecutor.execute("balance:charge:" + criteria.userId(), () ->
                aopForTransaction.run(() -> {
                    BalanceInfo info = retryService.chargeWithRetry(ChargeBalanceCommand.from(criteria));
                    eventPublisher.publishEvent(BalanceChargedEvent.from(criteria));
                    return BalanceResult.fromInfo(info);
                })
        );
    }


//    @Transactional
//    @DistributedLock(
//            key = "'balance:charge:' + #criteria.userId()",
//            waitTime = 10,        // 최대 10초까지 락 기다린다
//            leaseTime = 3,        // 락 유지시간은 3초만
//            timeUnit = TimeUnit.SECONDS
//    )
//    public BalanceResult charge(ChargeBalanceCriteria criteria) {
//        rateLimiter.validate(criteria.userId());
//
//        // 1. 중복 요청이면 잔액 반환
//        Optional<Balance> duplicated = historyUseCase.findIfDuplicatedRequest(criteria.requestId(), criteria.userId());
//        if (duplicated.isPresent()) {
//            log.warn("[멱등 요청] 이미 처리된 충전: requestId={}, userId={}", criteria.requestId(), criteria.userId());
//            return BalanceResult.fromInfo(BalanceInfo.from(duplicated.get()));
//        }
//
//        // 2. 충전 시도
//        BalanceInfo info = retryService.chargeWithRetry(ChargeBalanceCommand.from(criteria));
//
//        // 3. 히스토리 기록 (중복 시 skip)
////        historyUseCase.recordHistory(RecordBalanceHistoryCommand.of(criteria));
//        eventPublisher.publishEvent(BalanceChargedEvent.from(criteria));
//
//
//        return BalanceResult.fromInfo(info);
//    }


}

