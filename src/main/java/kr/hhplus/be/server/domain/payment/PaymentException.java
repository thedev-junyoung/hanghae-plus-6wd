package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;

public class PaymentException extends BusinessException {
    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static class UnsupportedMethodException extends BusinessException {
        public UnsupportedMethodException(String method) {
            super(ErrorCode.UNSUPPORTED_PAYMENT_METHOD, "지원되지 않는 결제 수단입니다: " + method);
        }
    }

    public static class NotFoundException extends BusinessException {
        public NotFoundException() {
            super(ErrorCode.PAYMENT_NOT_FOUND);
        }
        public NotFoundException(String pgTransactionId) {
            super(ErrorCode.PAYMENT_NOT_FOUND, "PG 거래 ID로 결제 정보를 찾을 수 없습니다: " + pgTransactionId);
        }
    }

    public static class MismatchedAmountException extends BusinessException {
        public MismatchedAmountException(long expected, long actual) {
            super(ErrorCode.INVALID_AMOUNT, "결제 금액이 올바르지 않습니다. expected=" + expected + ", actual=" + actual);
        }
    }

    public static class InvalidStateException extends BusinessException {
        public InvalidStateException(PaymentStatus status, String s) {
            super(ErrorCode.INVALID_PAYMENT_STATUS, String.format("결제 상태가 %s입니다. %s", status, s));
        }
    }

    public static class ExternalSystemException extends BusinessException {
        public ExternalSystemException(String reason) {
            super(ErrorCode.EXTERNAL_SYSTEM_ERROR, reason);
        }
    }

}
