package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponReader;
import org.springframework.stereotype.Repository;

@Repository
public class CouponReaderImpl implements CouponReader {
    @Override
    public Coupon findByCode(String code) {
        // TODO: 실제 DB 조회 로직 구현 예정
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
