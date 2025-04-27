package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponIssue;
import kr.hhplus.be.server.domain.coupon.CouponIssueWriter;
import org.springframework.stereotype.Repository;

@Repository
public class CouponIssueWriterImpl implements CouponIssueWriter {
    @Override
    public boolean hasIssued(Long userId, Long couponId) {
        // TODO: 사용자 중복 발급 여부 확인 로직 구현 예정
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CouponIssue save(CouponIssue issue) {
        return null;
    }

}
