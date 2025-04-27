package kr.hhplus.be.server.common.scheduler;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

class CouponExpirySchedulerTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponExpiryScheduler couponExpiryScheduler;


    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
    }

    @Test
    @DisplayName("만료된 쿠폰이 없으면 로그만 출력한다")
    void test_noExpiredCoupons() {
        // given
        when(couponRepository.findExpiredCoupons()).thenReturn(List.of());

        // when
        couponExpiryScheduler.logExpiredCoupons();

        // then
        verify(couponRepository).findExpiredCoupons();
    }

    @Test
    @DisplayName("만료된 쿠폰이 있으면 로그에 출력된다")
    void test_expiredCouponsExist() {
        // given
        Coupon expiredCoupon = mock(Coupon.class);
        when(expiredCoupon.isExpired(clock)).thenReturn(true);
        when(expiredCoupon.getCode()).thenReturn("EXPIRED-COUPON");
        when(expiredCoupon.getValidUntil()).thenReturn(LocalDateTime.now().minusDays(1));

        when(couponRepository.findExpiredCoupons()).thenReturn(List.of(expiredCoupon));

        // when
        couponExpiryScheduler.logExpiredCoupons();

        // then
        verify(couponRepository).findExpiredCoupons();
        verify(expiredCoupon).isExpired(clock);
    }
}
