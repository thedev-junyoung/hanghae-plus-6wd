package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;

public class OrderException extends BusinessException {
    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
    public static class EmptyItemException extends BusinessException {
        public EmptyItemException() {
            super(ErrorCode.INVALID_PARAMETER, "주문 항목은 비어 있을 수 없습니다.");
        }
    }
    public static class InvalidStateException extends BusinessException {

        public InvalidStateException(OrderStatus currentStatus, String reason) {
            super(ErrorCode.INVALID_ORDER_STATUS,
                    "현재 상태(" + currentStatus + ")에서는 이 작업을 수행할 수 없습니다: " + reason);
        }
    }

    public static class InvalidTotalAmountException extends BusinessException {
        public InvalidTotalAmountException(long expected, long actual) {
            super(ErrorCode.INVALID_AMOUNT, "총 금액이 올바르지 않습니다. expected=" + expected + ", actual=" + actual);
        }
    }
    public static class NotFoundException extends BusinessException {

        public NotFoundException(String orderId) {
            super(ErrorCode.ORDER_NOT_FOUND, "찾을 수없는 주문 ID: " + orderId);
        }
    }
}
