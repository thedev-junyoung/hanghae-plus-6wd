package kr.hhplus.be.server.domain.balance;

import kr.hhplus.be.server.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BalanceTest {

    @Test
    @DisplayName("잔액을 충전할 수 있다")
    void charge_balance() {
        Balance balance = Balance.createNew(1L, 100L, Money.wons(1000));
        balance.charge(Money.wons(1000));

        assertThat(balance.getAmount()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("잔액을 차감할 수 있다")
    void decrease_balance() {
        Balance balance = Balance.createNew(1L, 100L, Money.wons(1000));
        balance.decrease(Money.wons(300));

        assertThat(balance.getAmount()).isEqualTo(700L);
    }

    @Test
    @DisplayName("잔액을 차감할 때 잔액이 부족하면 예외가 발생한다")
    void throw_exception_when_balance_is_not_enough() {
        Balance balance = Balance.createNew(1L, 100L, Money.wons(500));

        assertThatThrownBy(() -> balance.decrease(Money.wons(600)))
                .isInstanceOf(BalanceException.NotEnoughBalanceException.class);
    }

    @Test
    @DisplayName("잔액이 충분한지 확인할 수 있다")
    void check_if_balance_is_enough() {
        Balance balance = Balance.createNew(1L, 100L, Money.wons(1000));

        assertThat(balance.hasEnough(Money.wons(1000))).isTrue();
        assertThat(balance.hasEnough(Money.wons(1500))).isFalse();
    }
}
