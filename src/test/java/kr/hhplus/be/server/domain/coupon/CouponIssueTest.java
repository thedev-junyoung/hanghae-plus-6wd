package kr.hhplus.be.server.domain.coupon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

class CouponIssueTest {

    private final String code = "TEST10";
    private final long userId = 1L;
    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2025-04-27T00:00:00Z"), ZoneId.of("UTC")
    );

    @Test
    @DisplayName("쿠폰 발급 시 수량이 차감되고 발급 시간이 고정된 시간으로 저장된다")
    void create_shouldDecreaseQuantityAndSetIssuedAt() {
        Coupon coupon = createValidCoupon();

        CouponIssue issue = CouponIssue.create(userId, coupon, fixedClock);

        assertThat(issue.getUserId()).isEqualTo(userId);
        assertThat(issue.getCoupon()).isEqualTo(coupon);
        assertThat(issue.isUsed()).isFalse();
        assertThat(issue.getIssuedAt()).isEqualTo(LocalDateTime.of(2025, 4, 27, 0, 0));
        assertThat(coupon.getRemainingQuantity()).isEqualTo(9);
    }

    @Test
    @DisplayName("markAsUsed 호출 시 isUsed가 true가 된다")
    void markAsUsed_shouldSetIsUsedTrue() {
        Coupon coupon = createValidCoupon();
        CouponIssue issue = CouponIssue.create(userId, coupon, fixedClock);

        issue.markAsUsed();

        assertThat(issue.isUsed()).isTrue();
    }

    @Test
    @DisplayName("이미 사용한 쿠폰에 markAsUsed 호출 시 예외 발생")
    void markAsUsed_shouldThrowIfAlreadyUsed() {
        Coupon coupon = createValidCoupon();
        CouponIssue issue = CouponIssue.create(userId, coupon, fixedClock);
        issue.markAsUsed();

        assertThatThrownBy(issue::markAsUsed)
                .isInstanceOf(CouponException.AlreadyIssuedException.class);
    }

    @Test
    @DisplayName("만료된 쿠폰 발급 시 CouponException 발생")
    void create_shouldThrowIfCouponExpired() {
        Coupon expiredCoupon = Coupon.create(code, CouponType.FIXED, 1000, 10,
                LocalDateTime.now(fixedClock).minusDays(10),
                LocalDateTime.now(fixedClock).minusDays(1));

        assertThatThrownBy(() -> CouponIssue.create(userId, expiredCoupon, fixedClock))
                .isInstanceOf(CouponException.ExpiredException.class);
    }

    private Coupon createValidCoupon() {
        return Coupon.create(code, CouponType.FIXED, 1000, 10,
                LocalDateTime.now(fixedClock).minusDays(1),
                LocalDateTime.now(fixedClock).plusDays(1));
    }
}
