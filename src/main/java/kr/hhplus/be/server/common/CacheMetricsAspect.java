package kr.hhplus.be.server.common;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheMetricsAspect {

    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;

    @Around("@annotation(cacheable)")
    public Object trackCacheHitMiss(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String cacheName = cacheable.value()[0]; // 첫 번째 캐시 이름만 추적
        String key = generateKey(joinPoint, cacheable.key());

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return joinPoint.proceed(); // fallback

        Object cachedValue = cache.get(key, Object.class);
        if (cachedValue != null) {
            meterRegistry.counter("cache.hit", "cache", cacheName).increment();
            return cachedValue;
        } else {
            meterRegistry.counter("cache.miss", "cache", cacheName).increment();
            return joinPoint.proceed();
        }
    }

    private String generateKey(JoinPoint joinPoint, String keySpEL) {
        // 가장 안전한 방법은 Spring Expression Parser로 파싱,
        // 여기선 단순화: days/limit 기준으로만 키 만들자
        Object[] args = joinPoint.getArgs();
        return Arrays.stream(args)
                .map(String::valueOf)
                .collect(Collectors.joining(":"));
    }
}
