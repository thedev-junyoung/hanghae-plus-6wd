package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.lock.AopForTransaction;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.DistributedLockExecutor;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.coupon.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class CouponService implements CouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final Clock clock;

    @DistributedLock(key = "#command.couponCode", prefix = "coupon:issue:")
    @Transactional
    public CouponResult issueLimitedCoupon(IssueLimitedCouponCommand command) {
        Coupon coupon = couponRepository.findByCode(command.couponCode());

        if (couponIssueRepository.hasIssued(command.userId(), coupon.getId())) {
            throw new CouponException.AlreadyIssuedException(command.userId(), command.couponCode());
        }

        coupon.validateUsable(clock);
        coupon.decreaseQuantity(clock);

        CouponIssue issue = CouponIssue.create(command.userId(), coupon, clock);
        couponIssueRepository.save(issue);

        return CouponResult.from(issue);
    }

    @Override
    @Transactional
    public ApplyCouponResult applyCoupon(ApplyCouponCommand command) {
        // 쿠폰 코드로 쿠폰 조회
        Coupon coupon = couponRepository.findByCode(command.couponCode());

        // 발급받은 쿠폰 이력 조회
        CouponIssue issue = couponIssueRepository.findByUserIdAndCouponId(command.userId(), coupon.getId())
                .orElseThrow(() -> new CouponException.NotIssuedException(command.userId(), command.couponCode()));

        // 쿠폰 유효성 및 사용 여부 체크
        coupon.validateUsable(clock);

        // 할인 금액 계산
        Money discount = issue.getCoupon().calculateDiscount(command.orderAmount());

        // 사용 처리
        issue.markAsUsed();

        return ApplyCouponResult.from(coupon, discount);
    }
}
