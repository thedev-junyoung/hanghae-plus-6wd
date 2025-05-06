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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class BalanceFacadeIntegrationTest {

    @Autowired
    private BalanceFacade balanceFacade;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;

    @Test
    @DisplayName("충전 성공 - DB에 이미 존재하는 유저, 이벤트 기반 이력 기록 확인")
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

        // then (await: 비동기 이벤트 반영 기다림)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Balance updated = balanceRepository.findByUserId(userId).orElseThrow();
            assertThat(updated.getAmount()).isEqualTo(beforeAmount + charge.value());

            List<BalanceHistory> histories = balanceHistoryRepository.findAllByUserId(userId);
            assertThat(histories).isNotEmpty();

            BalanceHistory latest = histories.get(histories.size() - 1);
            assertThat(latest.getAmount()).isEqualTo(charge.value());
            assertThat(latest.isChargeHistory()).isTrue();
            assertThat(latest.getReason()).isEqualTo("충전 테스트");
        });
    }
}
