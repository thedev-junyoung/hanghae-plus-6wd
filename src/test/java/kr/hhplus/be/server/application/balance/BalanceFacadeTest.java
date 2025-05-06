package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.lock.AopForTransaction;
import kr.hhplus.be.server.common.lock.DistributedLockExecutor;
import kr.hhplus.be.server.common.rate.InMemoryRateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class BalanceFacadeTest {

    @Mock
    private BalanceRetryService retryService;

    @Mock
    private BalanceHistoryUseCase historyUseCase;

    @Mock
    private InMemoryRateLimiter rateLimiter;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DistributedLockExecutor lockExecutor;

    @Mock
    private AopForTransaction aopForTransaction;

    @InjectMocks
    private BalanceFacade balanceFacade;

    @Test
    @DisplayName("충전 성공 시 이벤트가 발행된다")
    void charge_shouldPublishBalanceChargedEvent() throws Exception {
        // given
        String requestId = "REQ-123";
        ChargeBalanceCriteria criteria = new ChargeBalanceCriteria(1L, 10000L, "테스트 충전", requestId);
        ChargeBalanceCommand command = ChargeBalanceCommand.from(criteria);
        BalanceInfo fakeInfo = new BalanceInfo(1L, 20000L, LocalDateTime.now());

        when(historyUseCase.findIfDuplicatedRequest(requestId, 1L)).thenReturn(Optional.empty());
        when(retryService.chargeWithRetry(command)).thenReturn(fakeInfo);

        // 분산락 모킹
        when(lockExecutor.execute(eq("balance:charge:1"), any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<?> callable = invocation.getArgument(1);
                    return callable.call();
                });

        // AOP 트랜잭션 감싼 부분도 모킹
        when(aopForTransaction.run(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        // when
        BalanceResult result = balanceFacade.charge(criteria);

        // then
        verify(rateLimiter).validate(1L);
        verify(retryService).chargeWithRetry(command);
        verify(eventPublisher).publishEvent(BalanceChargedEvent.from(criteria));

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.balance()).isEqualTo(20000L);
    }

}
