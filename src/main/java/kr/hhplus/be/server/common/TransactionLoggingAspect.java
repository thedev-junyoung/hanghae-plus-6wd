package kr.hhplus.be.server.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Slf4j
public class TransactionLoggingAspect {

    @Around("@annotation(transactional)") // 트랜잭션 시작 및 종료 로깅
    public Object logTransaction(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        log.info(">>>>>>>>> Transaction started: {}", joinPoint.getSignature());
        try {
            Object result = joinPoint.proceed(); // 트랜잭션 메서드 실행
            log.info(">>>>>>>>> Transaction committed: {}", joinPoint.getSignature());
            return result;
        } catch (Exception e) {
            log.error(">>>>>>>>> Transaction rolled back due to an exception: {}", joinPoint.getSignature(), e);
            throw e;
        }
    }

    @AfterThrowing(pointcut = "@annotation(transactional)", throwing = "e")
    public void logTransactionRollback(Exception e, Transactional transactional) {
        log.error(">>>>>>>>> Transaction rollback triggered due to exception: {}", e.getMessage());
    }

    @AfterReturning("@annotation(transactional)")
    public void logTransactionCommit(Transactional transactional) {
        log.info(">>>>>>>>> Transaction committed successfully.");
    }
}