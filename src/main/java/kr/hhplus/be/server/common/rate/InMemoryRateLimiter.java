package kr.hhplus.be.server.common.rate;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("!test")
public class InMemoryRateLimiter {

    // 유저별 마지막 요청 시각
    private final ConcurrentHashMap<Long, Instant> lastRequestMap = new ConcurrentHashMap<>();

    private static final Duration LIMIT_INTERVAL = Duration.ofMillis(1000);

    public void validate(Long userId) {
        Instant now = Instant.now();
        Instant last = lastRequestMap.getOrDefault(userId, Instant.EPOCH);

        if (Duration.between(last, now).compareTo(LIMIT_INTERVAL) < 0) {
            throw new RateLimitExceededException("요청이 너무 빠릅니다. 잠시 후 다시 시도해주세요.");
        }

        // 유효한 요청이면 시간 갱신
        lastRequestMap.put(userId, now);
    }
}
