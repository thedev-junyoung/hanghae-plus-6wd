package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonDistributedLockExecutor implements DistributedLockExecutor {

    private final RedissonClient redissonClient;
    private static final long DEFAULT_WAIT_TIME = 5L;
//    private static final long DEFAULT_LEASE_TIME = 10L;

    @Override
    public <T> T execute(String key, Callable<T> action) {
        RLock lock = redissonClient.getLock(key);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(DEFAULT_WAIT_TIME, TimeUnit.SECONDS);
            System.out.println("Lock 획득 시도: " + key + " 결과: " + isLocked);
            if (!isLocked) {
                throw new IllegalStateException("락 획득 실패: " + key);
            }
            return action.call();
        } catch (RuntimeException e) {
            // 도메인 예외는 그대로 통과
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("예상치 못한 예외", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                log.info("락 해제 - key: {}", lock);
                lock.unlock();

            }
        }
    }


}
