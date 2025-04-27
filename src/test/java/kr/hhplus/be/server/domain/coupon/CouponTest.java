package kr.hhplus.be.server.domain.coupon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;

class CouponTest {

    private final Clock fixedClock = Clock.fixed(
            LocalDateTime.of(2025, 4, 27, 0, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC
    );
    private final LocalDateTime now = LocalDateTime.now(fixedClock);
    private final LocalDateTime yesterday = now.minusDays(1);
    private final LocalDateTime tomorrow = now.plusDays(1);

    @Test
    @DisplayName("만료된 쿠폰은 isExpired()가 true를 반환한다")
    void is_expired_should_return_true() {
        Coupon expired = Coupon.create("EXPIRED", CouponType.PERCENTAGE, 10, 100, now.minusDays(10), now.minusDays(1));
        assertThat(expired.isExpired(fixedClock)).isTrue();
    }

    @Test
    @DisplayName("유효한 기간의 쿠폰은 isExpired()가 false를 반환한다")
    void is_expired_should_return_false() {
        Coupon valid = Coupon.create("VALID", CouponType.PERCENTAGE, 10, 100, yesterday, tomorrow);
        assertThat(valid.isExpired(fixedClock)).isFalse();
    }

    @Test
    @DisplayName("잔여 수량이 0이면 isExhausted()는 true를 반환한다")
    void is_exhausted_should_return_true() {
        Coupon coupon = Coupon.create("LIMITED", CouponType.FIXED, 5000, 100, yesterday, tomorrow);
        for (int i = 0; i < 100; i++) {
            coupon.decreaseQuantity(fixedClock);
        }
        assertThat(coupon.isExhausted()).isTrue();
    }

    @Test
    @DisplayName("만료된 쿠폰은 validateUsable()에서 CouponExpiredException 발생")
    void validate_usable_should_throw_expired() {
        Coupon expired = Coupon.create("EXPIRED", CouponType.FIXED, 1000, 100, now.minusDays(5), now.minusMinutes(1));
        assertThatThrownBy(() -> expired.validateUsable(fixedClock))
                .isInstanceOf(CouponException.ExpiredException.class);
    }

    @Test
    @DisplayName("소진된 쿠폰은 validateUsable()에서 CouponAlreadyExhaustedException 발생")
    void validate_usable_should_throw_exhausted() {
        Coupon coupon = Coupon.create("EXHAUSTED", CouponType.FIXED, 1000, 1, yesterday, tomorrow);
        coupon.decreaseQuantity(fixedClock);

        assertThatThrownBy(() -> coupon.validateUsable(fixedClock))
                .isInstanceOf(CouponException.AlreadyExhaustedException.class);
    }

    @Test
    @DisplayName("유효한 쿠폰은 수량 감소가 정상 동작한다")
    void decrease_quantity_should_succeed() {
        Coupon coupon = Coupon.create("VALID", CouponType.FIXED, 1000, 10, yesterday, tomorrow);
        coupon.decreaseQuantity(fixedClock);
        assertThat(coupon.getRemainingQuantity()).isEqualTo(9);
    }

    @Test
    @DisplayName("PERCENTAGE 쿠폰은 올바르게 할인 적용한다")
    void percentage_coupon_should_apply_correct_discount() {
        int discounted = CouponType.PERCENTAGE.applyDiscount(10000, 10);
        assertThat(discounted).isEqualTo(9000);
    }

    @Test
    @DisplayName("FIXED 쿠폰은 고정 할인 적용 후 0원 미만 방지한다")
    void fixed_coupon_should_not_go_below_zero() {
        int discounted = CouponType.FIXED.applyDiscount(3000, 5000);
        assertThat(discounted).isEqualTo(0);
    }
}
