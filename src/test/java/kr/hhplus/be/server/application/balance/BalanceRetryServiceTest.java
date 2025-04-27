package kr.hhplus.be.server.application.balance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.support.RetryTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceRetryServiceTest {

    @Mock
    private BalanceUseCase balanceService;

    @InjectMocks
    private BalanceRetryService balanceRetryService;

    private ChargeBalanceCommand command;
    private BalanceInfo balanceInfo;

    @BeforeEach
    void setUp() {
        command = new ChargeBalanceCommand(1L, 10000L, "충전 테스트", "REQUEST_ID");
        balanceInfo = new BalanceInfo(1L, 10000L, LocalDateTime.now());

        // RetryTemplate 설정 (실제로 @Retryable이 사용하는 설정과 동일하게)
        RetryTemplate retryTemplate = new RetryTemplate();
    }

    @Test
    @DisplayName("충전 성공시 결과를 정상적으로 반환한다")
    void should_return_result_when_charge_succeeds() {
        // given
        when(balanceService.charge(command)).thenReturn(balanceInfo);

        // when
        BalanceInfo result = balanceRetryService.chargeWithRetry(command);

        // then
        assertThat(result).isEqualTo(balanceInfo);
        verify(balanceService, times(1)).charge(command);
    }

    @Test
    @DisplayName("낙관적 락 예외 발생 시 예외를 전파한다 (재시도 기능은 @Retryable에 의해 처리)")
    void should_propagate_optimistic_locking_failure() {
        // given
        OptimisticLockingFailureException exception = new OptimisticLockingFailureException("충돌 발생");
        when(balanceService.charge(command)).thenThrow(exception);

        // when & then
        assertThatThrownBy(() -> balanceRetryService.chargeWithRetry(command))
                .isInstanceOf(OptimisticLockingFailureException.class)
                .hasMessageContaining("충돌 발생");

        verify(balanceService, times(1)).charge(command);
        // 참고: 단위 테스트에서는 @Retryable 어노테이션의 재시도 동작은 검증할 수 없음
    }

    @Test
    @DisplayName("다른 예외가 발생하면 그대로 전파한다")
    void should_propagate_other_exceptions() {
        // given
        IllegalStateException exception = new IllegalStateException("다른 예외 발생");
        when(balanceService.charge(command)).thenThrow(exception);

        // when & then
        assertThatThrownBy(() -> balanceRetryService.chargeWithRetry(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("다른 예외 발생");

        verify(balanceService, times(1)).charge(command);
    }
}