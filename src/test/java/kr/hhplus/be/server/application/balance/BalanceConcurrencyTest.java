package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * 낙관적 락 기반의 동시성 충전 테스트 클래스.
 *
 * <p>이 테스트는 동일 사용자에 대해 동시에 충전 요청이 들어오는 상황을 시뮬레이션한다.</p>
 *
 * <p>적용된 동시성 제어 방식:</p>
 * <ul>
 *   <li>JPA의 `@Version`을 활용한 낙관적 락</li>
 *   <li>Spring Retry의 `@Retryable`로 충돌 시 재시도</li>
 *   <li>각 요청마다 `requestId`를 부여하여 멱등성 보장</li>
 *   <li>InMemoryRateLimiter로 짧은 시간 내 중복 요청 방지</li>
 * </ul>
 *
 * <p>검증 포인트:</p>
 * <ul>
 *   <li>모든 충전 요청이 중복 없이 정확하게 누적된다</li>
 *   <li>최종 잔액 = 성공한 요청 수 × 충전 금액</li>
 * </ul>
 */
@SpringBootTest
public class BalanceConcurrencyTest {

    @Autowired
    private BalanceFacade balanceFacade;

    @Autowired
    private BalanceRepository balanceRepository;

    private static final Long USER_ID = 777L;
    private static final int CONCURRENCY = 10;
    private static final long CHARGE_AMOUNT = 10_000L;

    private final AtomicInteger successCount = new AtomicInteger(0);

    @BeforeEach
    @Transactional
    void setUp() {
        initializeBalance(USER_ID);
    }

    @Test
    @DisplayName("여러 명이 동시에 충전 요청하면 잔액이 정확히 누적되어야 한다")
    void should_increase_balance_correctly_when_concurrent_charges_happen() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(CONCURRENCY);

        for (int i = 0; i < CONCURRENCY; i++) {
            int index = i;
            executor.execute(() -> {
                try {
                    // 각 요청마다 고유 requestId 부여
                    String requestId = "REQ-" + UUID.randomUUID();
                    ChargeBalanceCriteria criteria = ChargeBalanceCriteria.of(
                            USER_ID, CHARGE_AMOUNT, "동시성 테스트 충전", requestId
                    );

                    balanceFacade.charge(criteria);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("충전 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long finalAmount = balanceRepository.findByUserId(USER_ID)
                .orElseThrow(() -> new IllegalStateException("잔액 없음"))
                .getAmount();

        System.out.println("=== 최종 결과 ===");
        System.out.println("성공한 충전 수: " + successCount.get());
        System.out.println("예상 잔액: " + (successCount.get() * CHARGE_AMOUNT));
        System.out.println("실제 잔액: " + finalAmount);

        assertThat(finalAmount).isEqualTo(successCount.get() * CHARGE_AMOUNT);
    }

    @Transactional
    public void initializeBalance(Long userId) {
        balanceRepository.findByUserId(userId).ifPresentOrElse(
                balance -> {
                    balance.decrease(Money.wons(balance.getAmount()));
                    balanceRepository.save(balance);
                    System.out.println("기존 잔액 초기화 완료");
                },
                () -> {
                    balanceRepository.save(Balance.createNew(null, userId, Money.wons(0L)));
                    System.out.println("잔액 새로 생성됨");
                }
        );
    }
}
