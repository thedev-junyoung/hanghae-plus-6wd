package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.vo.Money;

public record ApplyCouponCommand(
        Long userId,
        String couponCode,
        Money orderAmount
) {
    public static ApplyCouponCommand of(Long userId, String couponCode, Money orderAmount) {
        return new ApplyCouponCommand(userId, couponCode, orderAmount);
    }
}
