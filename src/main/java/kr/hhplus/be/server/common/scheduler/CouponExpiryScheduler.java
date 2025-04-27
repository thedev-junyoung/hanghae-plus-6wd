package kr.hhplus.be.server.common.scheduler;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 매일 00시에 실행되는 만료 쿠폰 점검 스케줄러입니다.
 * 유효기간이 지난 쿠폰을 조회하여 로그를 출력합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpiryScheduler {

    private final CouponRepository couponRepository;
    private final Clock clock;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void logExpiredCoupons() {
        List<Coupon> expiredCoupons = couponRepository.findExpiredCoupons().stream()
                .filter(coupon -> coupon.isExpired(clock))
                .toList();

        if (expiredCoupons.isEmpty()) {
            log.info("[쿠폰 스케줄러] 만료된 쿠폰 없음.");
        } else {
            log.warn("[쿠폰 스케줄러] 만료된 쿠폰 목록:");
            expiredCoupons.forEach(coupon ->
                    log.warn("- 쿠폰 코드: {}, 만료일: {}", coupon.getCode(), coupon.getValidUntil()));
        }
    }
}
