package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceException;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import kr.hhplus.be.server.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

 
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    BalanceRepository balanceRepository;

    @InjectMocks
    BalanceService balanceService;

    @Mock
    BalanceHistoryRepository balanceHistoryRepository;

    @Test
    @DisplayName("잔액을 충전할 수 있다")
    void charge_success() {

        String requestId = "REQ-" + UUID.randomUUID();
        // given
        Balance existing = Balance.createNew(1L, 100L, Money.wons(1000));
        when(balanceRepository.findByUserId(100L)).thenReturn(Optional.of(existing));
        when(balanceRepository.save(eq(existing))).thenReturn(existing); // eq로 명시적 비교

        ChargeBalanceCommand command = new ChargeBalanceCommand(100L, 1000, "충전 테스트", requestId);

        // when
        BalanceInfo info = balanceService.charge(command);

        // then
        assertThat(info.amount()).isEqualTo(2000L);
        verify(balanceRepository).save(existing);
    }


    @Test
    @DisplayName("잔액을 차감할 수 있다")
    void decreaseSuccess() {
        // given
        Balance existing = Balance.createNew(1L, 100L, Money.wons(1000));
        when(balanceRepository.findByUserId(100L)).thenReturn(Optional.of(existing));
        when(balanceRepository.save(eq(existing))).thenReturn(existing);

        DecreaseBalanceCommand command = new DecreaseBalanceCommand(100L, 500);

        // when
        boolean result = balanceService.decreaseBalance(command);

        // then
        assertThat(result).isTrue();

        ArgumentCaptor<Balance> captor = ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());

        Balance saved = captor.getValue();
        assertThat(saved.getAmount()).isEqualTo(500); // 1000 - 500 = 500
    }

    @Test
    @DisplayName("잔액이 부족하면 예외가 발생한다")
    void decrease_fail_not_enough_balance() {
        // givena
        Balance existing = Balance.createNew(1L, 100L, Money.wons(300));
        when(balanceRepository.findByUserId(100L)).thenReturn(Optional.of(existing));

        DecreaseBalanceCommand command = new DecreaseBalanceCommand(100L, 500);

        // when & then
        assertThatThrownBy(() -> balanceService.decreaseBalance(command))
                .isInstanceOf(BalanceException.NotEnoughBalanceException.class);

        verify(balanceRepository, never()).save(any());
    }
    @Test
    @DisplayName("잔액을 조회할 수 있다")
    void getBalance_success() {
        Balance balance = Balance.createNew(1L, 100L, Money.wons(1500));
        when(balanceRepository.findByUserId(100L)).thenReturn(Optional.of(balance));

        BalanceResult result = balanceService.getBalance(100L);

        assertThat(result.userId()).isEqualTo(100L);
        assertThat(result.balance()).isEqualTo(1500L);
    }
}