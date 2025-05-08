package kr.hhplus.be.server.common.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 락을 걸 Redis Key
     */
    String key();

    /**
     * 락 대기 최대 시간 (초)
     */
    long waitTime() default 5L;

    /**
     * 락 점유 시간 (초)
     */
    long leaseTime() default 3L;

    /**
     * 락 대기 시간 단위
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;


    /**
     * 락을 걸 Redis Key Prefix
     */
    String prefix() default "";
}