package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(lockAnnotation)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock lockAnnotation) throws Throwable {
        String key = lockAnnotation.key();
        long waitTime = lockAnnotation.waitTime();
        long leaseTime = lockAnnotation.leaseTime();

        RLock lock = redissonClient.getLock(key);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new IllegalStateException("락 획득 실패: " + key);
            }
            return joinPoint.proceed();
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
