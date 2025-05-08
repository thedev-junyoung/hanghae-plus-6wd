package kr.hhplus.be.server.common.rate;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
public class DummyRateLimiter extends InMemoryRateLimiter {
    @Override
    public void validate(Long userId) {
        // 테스트 환경에서는 그냥 통과
    }
}
