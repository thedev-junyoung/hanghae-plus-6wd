package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.CouponIssue;

import java.time.LocalDateTime;

public record CouponResult(
        Long userCouponId,
        Long userId,
        String couponType,
        Integer discountRate,
        LocalDateTime issuedAt,
        LocalDateTime expiryDate
) {
    public static CouponResult from(CouponIssue issue) {
        return new CouponResult(
                issue.getId(),
                issue.getUserId(),
                issue.getCoupon().getType().name(),
                issue.getCoupon().getDiscountRate(),
                issue.getIssuedAt(),
                issue.getCoupon().getValidUntil()
        );
    }
}
