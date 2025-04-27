package kr.hhplus.be.server.infrastructure.coupon;


import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponException;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {
    private final CouponJpaRepository jpaRepository;

    @Override
    public void save(Coupon coupon) {
        jpaRepository.save(coupon);
    }

    @Override
    public Coupon findByCode(String code) {
        return jpaRepository.findByCode(code)
                .orElseThrow(() -> new CouponException.NotFoundException(code));
    }

    @Override
    public Coupon findByCodeForUpdate(String code) {
        return jpaRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new CouponException.NotFoundException(code));
    }

    @Override
    public Collection<Coupon> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Collection<Coupon> findExpiredCoupons() {
        return jpaRepository.findExpiredCoupons(LocalDateTime.now());
    }
}
