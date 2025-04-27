package kr.hhplus.be.server.domain.coupon;

import java.util.Optional;

public interface CouponIssueReader {
    boolean hasIssued(Long aLong, Long id);
    Optional<CouponIssue> findByUserIdAndCouponId(Long userId, Long couponId);

}
