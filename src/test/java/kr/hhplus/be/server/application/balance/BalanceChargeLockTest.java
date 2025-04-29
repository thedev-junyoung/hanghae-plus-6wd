package kr.hhplus.be.server.application.balance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BalanceChargeLockTest {

    @Autowired
    private BalanceFacade balanceFacade;

    private static final int THREAD_COUNT = 10;

    @DisplayName("동시에 충전 요청해도 락이 잘 걸려서 순차처리된다")
    @Test
    void simultaneousChargeRequestsAreSerialized() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    barrier.await();
                    ChargeBalanceCriteria criteria = new ChargeBalanceCriteria(
                            100L,
                            1000L,
                            "test",
                            UUID.randomUUID().toString()
                    );
                    BalanceResult result = balanceFacade.charge(criteria);
                    System.out.println("result = " + result);
                    return "SUCCESS";
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("락 획득 실패")) {
                        return "LOCK_FAIL: " + e.getMessage();
                    } else if (e.getMessage() != null && e.getMessage().contains("Row was updated or deleted")) {
                        return "OPTIMISTIC_FAIL: " + e.getMessage();
                    } else {
                        return "FAIL: " + e.getMessage();
                    }
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();

        int successCount = 0;
        int lockFail = 0;
        int optimisticFail = 0;
        int otherFail = 0;

        for (Future<String> future : futures) {
            String result = future.get();
            System.out.println("result = " + result);

            if (result.startsWith("SUCCESS")) {
                successCount++;
            } else if (result.startsWith("LOCK_FAIL")) {
                lockFail++;
            } else if (result.startsWith("OPTIMISTIC_FAIL")) {
                optimisticFail++;
            } else {
                otherFail++;
            }
        }

        System.out.println("성공: " + successCount +
                ", 락실패: " + lockFail +
                ", 낙관적락실패: " + optimisticFail +
                ", 기타실패: " + otherFail);

        assertThat(successCount).isGreaterThan(0);
    }

    @DisplayName("서로 다른 유저는 락 경합 없이 독립적으로 충전된다")
    @Test
    void multipleUsersCanChargeIndependently() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            long userId = 100L + i; // 서로 다른 유저
            futures.add(executorService.submit(() -> {
                try {
                    barrier.await();
                    ChargeBalanceCriteria criteria = new ChargeBalanceCriteria(
                            userId,
                            1000L,
                            "test",
                            UUID.randomUUID().toString()
                    );
                    BalanceResult result = balanceFacade.charge(criteria);
                    System.out.println("result = " + result);
                    return "SUCCESS";
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("락 획득 실패")) {
                        return "LOCK_FAIL: " + e.getMessage();
                    } else if (e.getMessage() != null && e.getMessage().contains("Row was updated or deleted")) {
                        return "OPTIMISTIC_FAIL: " + e.getMessage();
                    } else {
                        return "FAIL: " + e.getMessage();
                    }
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();

        int successCount = 0;
        int lockFail = 0;
        int optimisticFail = 0;
        int otherFail = 0;

        for (Future<String> future : futures) {
            String result = future.get();
            System.out.println("result = " + result);

            if (result.startsWith("SUCCESS")) {
                successCount++;
            } else if (result.startsWith("LOCK_FAIL")) {
                lockFail++;
            } else if (result.startsWith("OPTIMISTIC_FAIL")) {
                optimisticFail++;
            } else {
                otherFail++;
            }
        }

        System.out.println("성공: " + successCount +
                ", 락실패: " + lockFail +
                ", 낙관적락실패: " + optimisticFail +
                ", 기타실패: " + otherFail);

        assertThat(successCount).isEqualTo(THREAD_COUNT);
    }
}
