package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.lock.AopForTransaction;
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
    private final DistributedLockExecutor distributedLockExecutor;
    private final AopForTransaction aopForTransaction;

    @Override
    @Transactional
    public CouponResult issueLimitedCoupon(IssueLimitedCouponCommand command) {
        // 락 걸고 조회
//        Coupon coupon = couponRepository.findByCodeForUpdate(command.couponCode());
        Coupon coupon = couponRepository.findByCode(command.couponCode());

        // 중복 발급 방지
        if (couponIssueRepository.hasIssued(command.userId(), coupon.getId())) {
            throw new CouponException.AlreadyIssuedException(command.userId(), command.couponCode());
        }

//        // validateUsable에 clock 넘기기
//        coupon.validateUsable(clock);
//
//        // 도메인 책임으로 발급 생성 및 수량 차감
//        CouponIssue issue = CouponIssue.create(command.userId(), coupon, clock);

        // 분산락: 쿠폰 재조회 + 유효성 검사 + 재고 차감
        distributedLockExecutor.execute("coupon:issue:" + command.couponCode(), () -> {
            aopForTransaction.run(() -> {
                Coupon lockCoupon = couponRepository.findByCode(command.couponCode());
                lockCoupon.validateUsable(clock); // 여기서 CouponException 발생 가능
                lockCoupon.decreaseQuantity(clock);
                return null;
            });
            return null;
        });

        CouponIssue issue = CouponIssue.create(command.userId(), coupon, clock);

        // 저장
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
