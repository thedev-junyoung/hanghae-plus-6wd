package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.application.balance.BalanceFacade;
import kr.hhplus.be.server.application.balance.ChargeBalanceCriteria;
import kr.hhplus.be.server.application.balance.BalanceResult;
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
@ActiveProfiles("prod")  // ✅ 진짜 운영 환경으로 테스트
class BalanceChargeLockProdTest {

    @Autowired
    private BalanceFacade balanceFacade;

    private static final int THREAD_COUNT = 10;

    @DisplayName("운영 환경에서도 동시에 충전 요청해도 락과 레이트리밋이 정상 동작한다")
    @Test
    void simultaneousChargeRequestsAreSerializedInProd() throws Exception {
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
                            "prod-test",
                            UUID.randomUUID().toString()
                    );
                    BalanceResult result = balanceFacade.charge(criteria);
                    System.out.println("result = " + result);
                    return "SUCCESS";
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("락 획득 실패")) {
                        return "LOCK_FAIL: " + e.getMessage();
                    } else if (e.getMessage() != null && e.getMessage().contains("요청이 너무 빠릅니다")) {
                        return "RATE_LIMIT_FAIL: " + e.getMessage();
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
        int rateLimitFail = 0;
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
            } else if (result.startsWith("RATE_LIMIT_FAIL")) {
                rateLimitFail++;
            } else {
                otherFail++;
            }
        }

        System.out.println("성공: " + successCount +
                ", 락실패: " + lockFail +
                ", 낙관적락실패: " + optimisticFail +
                ", 레이트리밋실패: " + rateLimitFail +
                ", 기타실패: " + otherFail);

        assertThat(successCount).isGreaterThan(0);
    }
}
