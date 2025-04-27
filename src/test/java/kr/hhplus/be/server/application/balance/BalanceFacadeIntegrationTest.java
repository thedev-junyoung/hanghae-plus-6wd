package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
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
        Long userId = 100L; // DB에 이미 존재하는 유저
        Money charge = Money.wons(5_000);

        Balance original = balanceRepository.findByUserId(userId).orElseThrow();
        long beforeAmount = original.getAmount();

        String requestId = "REQ-" + UUID.randomUUID();
        ChargeBalanceCriteria criteria = ChargeBalanceCriteria.of(userId, charge.value(), "충전 테스트", requestId);

        // when
        balanceFacade.charge(criteria);

        // then
        Balance updated = balanceRepository.findByUserId(userId).orElseThrow();
        assertThat(updated.getAmount()).isEqualTo(beforeAmount + charge.value());

        List<BalanceHistory> histories = balanceHistoryRepository.findAllByUserId(userId);
        BalanceHistory latest = histories.get(histories.size() - 1); // 최신 기록

        assertThat(latest.getAmount()).isEqualTo(charge.value());
        assertThat(latest.isChargeHistory()).isTrue();
        assertThat(latest.getReason()).isEqualTo("충전 테스트");
    }
}