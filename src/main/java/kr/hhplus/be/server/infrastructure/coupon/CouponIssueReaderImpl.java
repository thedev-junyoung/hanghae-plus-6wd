package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.CouponIssue;
import kr.hhplus.be.server.domain.coupon.CouponIssueReader;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CouponIssueReaderImpl implements CouponIssueReader {
    @Override
    public boolean hasIssued(Long aLong, Long id) {
        return false;
    }

    @Override
    public Optional<CouponIssue> findByUserIdAndCouponId(Long userId, Long couponId) {
        return Optional.empty();
    }
}
