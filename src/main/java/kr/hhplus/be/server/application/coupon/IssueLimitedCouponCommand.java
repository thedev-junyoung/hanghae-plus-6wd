package kr.hhplus.be.server.application.coupon;

public record IssueLimitedCouponCommand(
        Long userId,
        String couponCode
) {
    public static IssueLimitedCouponCommand of(Long userId, String couponCode) {
        return new IssueLimitedCouponCommand(userId, couponCode);
    }
}
