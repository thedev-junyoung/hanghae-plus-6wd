package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemTest {

    @Test
    @DisplayName("총 가격을 계산할 수 있다")
    void calculate_total_price() {
        // given
        OrderItem item = OrderItem.of(1L, 3, 270, Money.wons(10000));

        // when
        Money total = item.calculateTotal();

        // then
        assertThat(total).isEqualTo(Money.wons(30000));
    }
}
