package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;

public class CouponException extends BusinessException {
    public CouponException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static class AlreadyExhaustedException extends BusinessException {
        public AlreadyExhaustedException() {
            super(ErrorCode.COUPON_EXHAUSTED);
        }
    }

    public static class AlreadyIssuedException extends BusinessException {
        public AlreadyIssuedException(Long userId, String code) {
            super(ErrorCode.COUPON_ALREADY_USED, "이미 발급받은 쿠폰입니다: userId=" + userId + ", code=" + code);
        }
    }
    public static class ExpiredException extends BusinessException {

        public ExpiredException() {
            super(ErrorCode.COUPON_EXPIRED, "쿠폰이 만료되었습니다: ");
        }
    }

    public static class NotIssuedException extends BusinessException {
        public NotIssuedException(Long userId, String code) {
            super(ErrorCode.COUPON_NOT_ISSUED, "발급되지 않은 쿠폰입니다: userId=" + userId + ", code=" + code);
        }
    }
    public static class NotFoundException extends BusinessException {
        public NotFoundException(String code) {
            super(ErrorCode.COUPON_NOT_FOUND, "쿠폰을 찾을 수 없습니다: code=" + code);
        }
    }

}
