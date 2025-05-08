package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.application.order.CreateOrderCommand;
import kr.hhplus.be.server.common.lock.AopForTransaction;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.DistributedLockExecutor;
import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.coupon.*;
import kr.hhplus.be.server.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService implements CouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final Clock clock;



    @DistributedLock(key = "#command.couponCode", prefix = "coupon:issue:")
    @Transactional
    public CouponResult issueLimitedCoupon(IssueLimitedCouponCommand command) {

        log.info("[비즈니스 로직 시작: 쿠폰 발급] userId={}, couponCode={}", command.userId(), command.couponCode());

        Coupon coupon = couponRepository.findByCode(command.couponCode());

        if (couponIssueRepository.hasIssued(command.userId(), coupon.getId())) {
            log.info("[중복 발급 차단] userId={}, couponCode={}", command.userId(), command.couponCode());
            throw new CouponException.AlreadyIssuedException(command.userId(), command.couponCode());
        }

        coupon.validateUsable(clock);
        coupon.decreaseQuantity(clock);
        log.info("[재고 차감 완료] couponCode={}, 남은 수량={}", coupon.getCode(), coupon.getRemainingQuantity());

        CouponIssue issue = CouponIssue.create(command.userId(), coupon, clock);

        log.info("[DB 저장 직전] userId={}, couponId={}", command.userId(), coupon.getId());

        couponIssueRepository.save(issue);

        log.info("[비즈니스 로직 끝] 쿠폰 발급 성공 userId={}, couponCode={}", command.userId(), command.couponCode());

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

    public Money calculateDiscountedTotal(CreateOrderCommand command, List<OrderItem> items) {
        Money total = items.stream()
                .map(OrderItem::calculateTotal)
                .reduce(Money.ZERO, Money::add);

        if (!command.hasCouponCode()) {
            return total;
        }

        Coupon coupon = couponRepository.findByCode(command.couponCode());

        CouponIssue issue = couponIssueRepository.findByUserIdAndCouponId(command.userId(), coupon.getId())
                .orElseThrow(() -> new CouponException.NotIssuedException(command.userId(), command.couponCode()));

        coupon.validateUsable(clock);

        Money discount = issue.getCoupon().calculateDiscount(total);
        return total.subtract(discount);
    }

}
