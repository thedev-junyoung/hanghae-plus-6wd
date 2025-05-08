package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(lockAnnotation)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock lockAnnotation) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        String resolvedKey = parser.parseExpression(lockAnnotation.key()).getValue(context, String.class);
        String lockKey = lockAnnotation.prefix() + resolvedKey;

        long waitTime = lockAnnotation.waitTime();
        long leaseTime = lockAnnotation.leaseTime();

        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        try {
            log.info("락 획득 - key: {}", lockKey);
            isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new IllegalStateException("락 획득 실패: " + lockKey);
            }
            // 락을 획득한 다음, 트랜잭션 분리해서 비즈니스 로직 실행
            return aopForTransaction.proceed(joinPoint);
        } catch (Exception e) {
            log.error("분산락 수행 중 예외 발생 - key: {}", lockKey, e);
            throw e;
        }finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                log.info("락 해제 - key: {}", lockKey);
                lock.unlock();
            }
        }
    }
}
