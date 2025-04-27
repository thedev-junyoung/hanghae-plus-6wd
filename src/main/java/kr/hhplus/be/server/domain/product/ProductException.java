package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;

public class ProductException extends BusinessException {
    public ProductException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static class DescriptionTooLongException extends BusinessException {
        public DescriptionTooLongException(int maxLength) {
            super(ErrorCode.INVALID_INPUT_VALUE, "설명은 " + maxLength + "자 이하만 가능합니다.");
        }
    }

    public static class InsufficientStockException extends BusinessException {

        public InsufficientStockException(String message) {
            super(ErrorCode.INSUFFICIENT_STOCK, message);
        }
        public InsufficientStockException() {
            super(ErrorCode.INSUFFICIENT_STOCK);
        }
    }

    public static class NotFoundException extends BusinessException {
        public NotFoundException(Long productId) {
            super(ErrorCode.ENTITY_NOT_FOUND, "상품을 찾을 수 없습니다. (상품 ID: " + productId + ")");
        }
    }

    public static class NotReleasedException extends BusinessException {
        public NotReleasedException(Long productId) {
            super(ErrorCode.INVALID_INPUT_VALUE, "아직 출시되지 않은 상품입니다. (상품 ID: " + productId + ")");
        }
    }
    public static class OutOfStockException extends BusinessException {
        public OutOfStockException(Long productId) {
            super(ErrorCode.INSUFFICIENT_STOCK, "재고가 부족한 상품입니다. (상품 ID: " + productId + ")");
        }
    }
}
