package kr.hhplus.be.server.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("금액을 더할 수 있다")
    void addMoney() {
        Money a = Money.wons(1000);
        Money b = Money.wons(2000);

        Money result = a.add(b);

        assertThat(result).isEqualTo(Money.wons(3000));
    }

    @Test
    @DisplayName("금액을 뺄 수 있다")
    void subtractMoney() {
        Money a = Money.wons(5000);
        Money b = Money.wons(1500);

        Money result = a.subtract(b);

        assertThat(result).isEqualTo(Money.wons(3500));
    }

    @Test
    @DisplayName("금액을 곱할 수 있다")
    void multiplyMoney() {
        Money money = Money.wons(3000);

        Money result = money.multiply(3);

        assertThat(result).isEqualTo(Money.wons(9000));
    }

    @Test
    @DisplayName("금액 비교가 가능하다 (isGreaterThanOrEqual)")
    void compareMoney() {
        Money a = Money.wons(5000);
        Money b = Money.wons(3000);
        Money c = Money.wons(5000);

        assertThat(a.isGreaterThanOrEqual(b)).isTrue();
        assertThat(a.isGreaterThanOrEqual(c)).isTrue();
        assertThat(b.isGreaterThanOrEqual(a)).isFalse();
    }

    @Test
    @DisplayName("금액 VO는 값 기반 동등 비교가 가능하다")
    void moneyEqualsAndHashcode() {
        Money a = Money.wons(10000);
        Money b = Money.wons(10000);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
