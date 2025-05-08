package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis 기반 분산락을 활용한 잔액 충전 동시성 테스트.
 *
 * <p>동일 사용자의 충전 요청이 동시에 발생할 때 Race Condition 없이 정확하게 처리되는지 검증한다.</p>
 *
 * <p><b>적용된 동시성 제어 전략:</b></p>
 * <ul>
 *   <li><b>Redisson 분산락</b> : key = "balance:charge:{userId}". 락 획득 후에만 충전 로직 진입</li>
 *   <li><b>트랜잭션 경계</b> : 락 획득 후 AOP를 통해 @Transactional(REQUIRES_NEW) 시작</li>
 *   <li><b>비즈니스 로직</b> : 잔액 조회 및 금액 충전 → 저장</li>
 *   <li><b>멱등성 보장</b> : requestId 기반으로 중복 요청 차단</li>
 *   <li><b>Rate Limiting</b> : InMemoryRateLimiter로 과도한 반복 요청 차단</li>
 *   <li><b>이벤트 발행</b> : 커밋 후 BalanceChargedEvent 발행 → 충전 이력 기록</li>
 * </ul>
 *
 * <p><b>검증 포인트:</b></p>
 * <ul>
 *   <li>모든 충전 요청이 정확히 한 번씩만 처리된다.</li>
 *   <li>최종 잔액 = 성공한 요청 수 × 충전 금액.</li>
 *   <li>로그를 통해 락 → 트랜잭션 → 로직 → 커밋 → 이벤트 → 락 해제 순서를 확인한다.</li>
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
    @Test
    @DisplayName("동일 유저 1명에 대해 동시 충전 요청이 정확히 처리된다")
    void singleUser_multipleRequests() throws Exception {
        long userId = 777L;
        int threadCount = 50; // 같은 유저 50개 요청

        executeConcurrentChargeTest(userId, threadCount);
    }

    @Test
    @DisplayName("다수 유저가 동시에 충전 요청하면 각각 정확히 처리된다")
    void multipleUsers_concurrentRequests() throws Exception {
        int userCount = 10;
        int requestsPerUser = 10;

        ExecutorService executor = Executors.newFixedThreadPool(userCount * requestsPerUser);
        CountDownLatch latch = new CountDownLatch(userCount * requestsPerUser);

        for (long userId = 1000; userId < 1000 + userCount; userId++) {
            initializeBalance(userId);

            for (int i = 0; i < requestsPerUser; i++) {
                long finalUserId = userId;
                executor.execute(() -> {
                    try {
                        String requestId = "REQ-" + UUID.randomUUID();
                        ChargeBalanceCriteria criteria = ChargeBalanceCriteria.of(
                                finalUserId, CHARGE_AMOUNT, "멀티유저 동시 충전 테스트", requestId
                        );

                        balanceFacade.charge(criteria);
                    } catch (Exception e) {
                        System.out.println("충전 실패: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();
        executor.shutdown();

        // 검증 - 각 유저의 잔액 확인
        for (long userId = 1000; userId < 1000 + userCount; userId++) {
            long amount = balanceRepository.findByUserId(userId)
                    .orElseThrow().getAmount();
            assertThat(amount).isEqualTo(CHARGE_AMOUNT * requestsPerUser);
        }
    }

    private void executeConcurrentChargeTest(Long userId, int requestCount) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch latch = new CountDownLatch(requestCount);

        for (int i = 0; i < requestCount; i++) {
            executor.execute(() -> {
                try {
                    String requestId = "REQ-" + UUID.randomUUID();
                    ChargeBalanceCriteria criteria = ChargeBalanceCriteria.of(
                            userId, CHARGE_AMOUNT, "동시성 테스트 충전", requestId
                    );

                    balanceFacade.charge(criteria);
                } catch (Exception e) {
                    System.out.println("충전 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long finalAmount = balanceRepository.findByUserId(userId)
                .orElseThrow().getAmount();

        System.out.println("최종 잔액: " + finalAmount);
        assertThat(finalAmount).isEqualTo(CHARGE_AMOUNT * requestCount);
    }


    public void initializeBalance(Long userId) {
        balanceRepository.findByUserId(userId).ifPresentOrElse(
                balance -> {
                    balance.decrease(Money.wons(balance.getAmount()));
                    balanceRepository.save(balance);
                    System.out.println("기존 잔액 초기화 완료");
                },
                () -> {
                    balanceRepository.save(Balance.createNew(userId, Money.wons(0L)));
                    System.out.println("잔액 새로 생성됨");
                }
        );
    }
}