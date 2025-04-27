package kr.hhplus.be.server.common.rate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InMemoryRateLimiterTest {

    private final InMemoryRateLimiter rateLimiter = new InMemoryRateLimiter();

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("첫 번째 요청은 통과되어야 한다")
    void first_request_should_pass() {
        assertDoesNotThrow(() -> rateLimiter.validate(USER_ID));
    }

    @Test
    @DisplayName("제한 시간 내 연속 요청은 차단되어야 한다")
    void second_request_within_limit_interval_should_fail() {
        // 첫 번째 요청 → 통과
        rateLimiter.validate(USER_ID);

        // 두 번째 요청 (즉시) → 예외 발생
        assertThatThrownBy(() -> rateLimiter.validate(USER_ID))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("요청이 너무 빠릅니다");
    }

    @Test
    @DisplayName("제한 시간 이후 요청은 통과되어야 한다")
    void request_after_limit_interval_should_pass() throws InterruptedException {
        // 첫 요청
        rateLimiter.validate(USER_ID);

        // 제한 시간(800ms) 이후 재요청
        Thread.sleep(850);

        assertDoesNotThrow(() -> rateLimiter.validate(USER_ID));
    }
}
