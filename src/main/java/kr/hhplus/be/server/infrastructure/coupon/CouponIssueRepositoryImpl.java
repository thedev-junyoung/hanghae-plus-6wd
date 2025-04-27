package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.CouponIssue;
import kr.hhplus.be.server.domain.coupon.CouponIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponIssueRepositoryImpl implements CouponIssueRepository {

    private final CouponIssueJpaRepository jpaRepository;

    @Override
    public CouponIssue save(CouponIssue issue) {
        return jpaRepository.save(issue);
    }

    @Override
    public boolean hasIssued(Long userId, Long couponId) {
        return jpaRepository.existsByUserIdAndCoupon_Id(userId, couponId);
    }

    @Override
    public Optional<CouponIssue> findByUserIdAndCouponId(Long userId, Long couponId) {
        return jpaRepository.findByUserIdAndCoupon_Id(userId, couponId);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByCouponCode(String code) {
        return jpaRepository.countByCouponCode(code);
    }
}
