package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.coupon.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponIssueRepository couponIssueRepository;

    private CouponService couponService; // <-- 직접 생성할거야

    private Clock fixedClock;

    private final String couponCode = "TEST10";
    private final long userId = 1L;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2025-04-27T00:00:00Z"), ZoneId.of("UTC"));
        couponService = new CouponService(couponRepository, couponIssueRepository, fixedClock); // <-- 여기!!
    }

    @Test
    @DisplayName("쿠폰 정상 발급 성공")
    void issueCoupon_success() {
        Coupon coupon = createValidCoupon();
        IssueLimitedCouponCommand command = new IssueLimitedCouponCommand(userId, couponCode);

        given(couponRepository.findByCodeForUpdate(couponCode)).willReturn(coupon);
        given(couponIssueRepository.hasIssued(userId, coupon.getId())).willReturn(false);

        CouponResult result = couponService.issueLimitedCoupon(command);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);

        ArgumentCaptor<CouponIssue> captor = ArgumentCaptor.forClass(CouponIssue.class);
        verify(couponIssueRepository).save(captor.capture());

        CouponIssue saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getCoupon()).isEqualTo(coupon);
    }

    @Test
    @DisplayName("이미 쿠폰을 발급받은 경우 예외 발생")
    void issueCoupon_fail_ifAlreadyIssued() {
        Coupon coupon = createValidCoupon();
        given(couponRepository.findByCodeForUpdate(couponCode)).willReturn(coupon);
        given(couponIssueRepository.hasIssued(userId, coupon.getId())).willReturn(true);

        assertThrows(CouponException.AlreadyIssuedException.class, () ->
                couponService.issueLimitedCoupon(new IssueLimitedCouponCommand(userId, couponCode)));
    }

    @Test
    @DisplayName("만료된 쿠폰일 경우 예외 발생")
    void issueCoupon_fail_ifExpired() {
        LocalDateTime now = LocalDateTime.now(fixedClock);
        Coupon expiredCoupon = Coupon.create(
                couponCode,
                CouponType.FIXED,
                1000,
                10,
                now.minusDays(10),
                now.minusDays(1)
        );

        given(couponRepository.findByCodeForUpdate(couponCode)).willReturn(expiredCoupon);

        assertThrows(CouponException.ExpiredException.class, () ->
                couponService.issueLimitedCoupon(new IssueLimitedCouponCommand(userId, couponCode))
        );
    }

    @Test
    @DisplayName("수량 소진된 쿠폰일 경우 예외 발생")
    void issueCoupon_fail_ifSoldOut() {
        LocalDateTime now = LocalDateTime.now(fixedClock);
        Coupon soldOutCoupon = new Coupon(
                couponCode,
                CouponType.PERCENTAGE,
                20,
                10,
                0, // 재고 0
                now.minusDays(1),
                now.plusDays(1)
        );

        given(couponRepository.findByCodeForUpdate(couponCode)).willReturn(soldOutCoupon);

        assertThrows(CouponException.AlreadyExhaustedException.class, () ->
                couponService.issueLimitedCoupon(new IssueLimitedCouponCommand(userId, couponCode))
        );
    }

    @Test
    @DisplayName("쿠폰 적용 성공")
    void applyCoupon_success() {
        Coupon coupon = createValidCoupon();
        Money orderAmount = Money.wons(10000);
        Money expectedDiscount = coupon.calculateDiscount(orderAmount);

        CouponIssue issue = CouponIssue.create(userId, coupon, fixedClock);

        given(couponRepository.findByCode(couponCode)).willReturn(coupon);
        given(couponIssueRepository.findByUserIdAndCouponId(userId, coupon.getId()))
                .willReturn(java.util.Optional.of(issue));

        ApplyCouponResult result = couponService.applyCoupon(
                new ApplyCouponCommand(userId, couponCode, orderAmount)
        );

        assertThat(result.couponCode()).isEqualTo(couponCode);
        assertThat(result.discountAmount()).isEqualTo(expectedDiscount);
    }

    @Test
    @DisplayName("쿠폰 미발급 상태에서 쿠폰 적용 시 예외 발생")
    void applyCoupon_fail_ifNotIssued() {
        Coupon coupon = createValidCoupon();

        given(couponRepository.findByCode(couponCode)).willReturn(coupon);
        given(couponIssueRepository.findByUserIdAndCouponId(userId, coupon.getId()))
                .willReturn(java.util.Optional.empty());

        assertThrows(CouponException.NotIssuedException.class, () ->
                couponService.applyCoupon(new ApplyCouponCommand(userId, couponCode, Money.wons(10000))));
    }

    private Coupon createValidCoupon() {
        LocalDateTime now = LocalDateTime.now(fixedClock);
        return Coupon.create(
                couponCode,
                CouponType.PERCENTAGE,
                10,
                100,
                now.minusDays(1),
                now.plusDays(1)
        );
    }
}

