package kr.hhplus.be.server.application.balance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;


import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.balance.BalanceRepository;

import java.util.UUID;

@SpringBootTest
@Transactional
class BalanceServiceIntegrationTest {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private BalanceRepository balanceRepository;



    @Test
    @DisplayName("잔액 충전이 성공하면 실제 금액이 증가한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void charge_shouldIncreaseBalance() {
        // given
        Long userId = 100L;
        Money chargeAmount = Money.wons(10_000);
        long before = balanceRepository.findByUserId(userId).orElseThrow().getAmount();

        String requestId = UUID.randomUUID().toString(); // 반드시 매번 새로 생성

        // when
        balanceService.charge(new ChargeBalanceCommand(userId, chargeAmount.value(), "충전 테스트", requestId));


        // then
        long after = balanceRepository.findByUserId(userId).orElseThrow().getAmount();
        assertThat(after).isEqualTo(before + chargeAmount.value());
    }


    @Test
    @DisplayName("잔액 차감이 성공하면 실제 금액이 감소한다")
    void decrease_shouldReduceBalance() {
        // given
        Long userId = 100L;
        Money decreaseAmount = Money.wons(5000);
        long before = balanceRepository.findByUserId(userId).orElseThrow().getAmount();

        // when
        balanceService.decreaseBalance(new DecreaseBalanceCommand(userId, decreaseAmount.value()));

        // then
        long after = balanceRepository.findByUserId(userId).orElseThrow().getAmount();
        assertThat(after).isEqualTo(before - decreaseAmount.value());
    }
    @Test
    @DisplayName("잔액 조회가 성공하면 정확한 값을 반환한다")
    void getBalance_shouldReturnCorrectAmount() {
        // given
        Long userId = 100L;

        // when
        BalanceResult result = balanceService.getBalance(userId);

        // then
        assertThat(result.balance()).isEqualTo(
                balanceRepository.findByUserId(userId).orElseThrow().getAmount()
        );
    }


}