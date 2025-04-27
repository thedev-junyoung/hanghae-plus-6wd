package kr.hhplus.be.server.domain.coupon;

public interface CouponIssueWriter {
    CouponIssue save(CouponIssue issue); // 바꿔주세요
    boolean hasIssued(Long userId, Long couponId);
}