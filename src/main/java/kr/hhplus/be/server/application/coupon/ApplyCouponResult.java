package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.vo.Money;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponType;


public record ApplyCouponResult(
        String couponCode,
        CouponType couponType,
        int discountRate,
        Money discountAmount
) {
    public static ApplyCouponResult from(Coupon coupon, Money discountAmount) {
        return new ApplyCouponResult(
                coupon.getCode(),
                coupon.getType(),
                coupon.getDiscountRate(),
                discountAmount
        );
    }
}

