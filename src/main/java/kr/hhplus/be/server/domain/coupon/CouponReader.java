package kr.hhplus.be.server.domain.coupon;

public interface CouponReader {
    Coupon findByCode(String code); // 못 찾으면 CouponNotFoundException
}
