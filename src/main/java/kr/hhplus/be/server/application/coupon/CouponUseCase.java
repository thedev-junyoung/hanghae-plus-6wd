package kr.hhplus.be.server.application.coupon;

public interface CouponUseCase {

    /**
     * 선착순 쿠폰을 발급합니다.
     */
    CouponResult issueLimitedCoupon(IssueLimitedCouponCommand command);

    /**
     * 주문 시 쿠폰의 유효성을 검증하고 할인 금액을 계산합니다.
     */
    ApplyCouponResult applyCoupon(ApplyCouponCommand command);
}

