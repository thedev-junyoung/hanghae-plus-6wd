package kr.hhplus.be.server.common.lock;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
@Slf4j
public class AopForTransaction {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
        Object result = joinPoint.proceed();
        log.info("[트랜잭션 종료 - 커밋] {}", joinPoint.getSignature());
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T run(Supplier<T> supplier) {
        log.info("[트랜잭션 시작] AopForTransaction.run");
        T result = supplier.get();
        log.info("[트랜잭션 종료 - 커밋] AopForTransaction.run");
        return result;
    }

}
