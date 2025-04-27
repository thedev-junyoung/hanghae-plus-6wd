package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.coupon.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 쿠폰 서비스 통합 테스트
 * - 쿠폰 발급 및 사용 관련 기능을 통합적으로 테스트
 * - DB에 실제로 저장되는지 확인
 * coupon
 * id:3
 * code:TESTONLY1000
 * discount_rate:1000
 */
@SpringBootTest
@Transactional
class CouponServiceIntegrationTest {

    @Autowired
    CouponUseCase couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponIssueRepository couponIssueRepository;

    Long userId = ThreadLocalRandom.current().nextLong(10_000_000, 99_999_999);
    String couponCode = "TESTONLY1000";

    @Test
    @DisplayName("쿠폰 발급 시 coupon_issue 테이블에 저장되고, 결과가 올바르다")
    void issueLimitedCoupon_savedToDatabase() {
        // given
        IssueLimitedCouponCommand command = IssueLimitedCouponCommand.of(userId, couponCode);

        // when
        CouponResult result = couponService.issueLimitedCoupon(command);

        // then: 1차 - 반환된 result 자체 검증
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.couponType()).isEqualTo("FIXED"); // 혹은 PERCENTAGE
        assertThat(result.discountRate()).isEqualTo(1000); // DB에 저장된 값

        // then: 2차 - 실제 DB에도 저장되었는지 확인
        Coupon coupon = couponRepository.findByCode(couponCode);
        CouponIssue issue = couponIssueRepository.findByUserIdAndCouponId(userId, coupon.getId())
                .orElseThrow(() -> new AssertionError("coupon_issue not saved"));

        assertThat(issue.getUserId()).isEqualTo(userId);
        assertThat(issue.getCoupon().getCode()).isEqualTo(couponCode);
        assertThat(issue.isUsed()).isFalse();
    }


    @Test
    @DisplayName("쿠폰을 적용하면 isUsed 플래그가 true로 변경된다")
    void applyCoupon_marksAsUsed() {
        // given
        couponService.issueLimitedCoupon(IssueLimitedCouponCommand.of(userId, couponCode));
        ApplyCouponCommand command = ApplyCouponCommand.of(userId, couponCode, Money.from(20_000L));

        // when
        couponService.applyCoupon(command);

        // then
        Coupon coupon = couponRepository.findByCode(couponCode);
        CouponIssue issue = couponIssueRepository.findByUserIdAndCouponId(userId, coupon.getId())
                .orElseThrow(() -> new AssertionError("coupon_issue not found"));

        assertThat(issue.isUsed()).isTrue();
    }

    @Test
    @DisplayName("쿠폰 발급 시 남은 수량이 1 감소한다")
    void issueLimitedCoupon_decreaseQuantity() {
        // given
        Coupon before = couponRepository.findByCode(couponCode);
        int prevRemaining = before.getRemainingQuantity();

        // when
        couponService.issueLimitedCoupon(IssueLimitedCouponCommand.of(userId, couponCode));

        // then
        Coupon after = couponRepository.findByCode(couponCode);
        assertThat(after.getRemainingQuantity()).isEqualTo(prevRemaining - 1);
    }

}

