package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BalanceFacadeIntegrationTest {

    @Autowired
    private BalanceFacade balanceFacade;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;

    @Test
    @DisplayName("충전 성공 - DB에 이미 존재하는 유저")
    void charge_success_using_seeded_data() {
        // given
        Long userId = 100L;
        Money charge = Money.wons(5_000);

        Balance original = balanceRepository.findByUserId(userId).orElseThrow();
        long beforeAmount = original.getAmount();

        String requestId = "REQ-" + UUID.randomUUID();
        ChargeBalanceCriteria criteria = ChargeBalanceCriteria.of(userId, charge.value(), "충전 테스트", requestId);

        // when
        balanceFacade.charge(criteria);
        Balance updated = balanceRepository.findByUserId(userId).orElseThrow();
        assertThat(updated.getAmount()).isEqualTo(beforeAmount + charge.value());

    }
}
