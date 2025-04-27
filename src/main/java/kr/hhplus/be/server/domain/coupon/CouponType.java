package kr.hhplus.be.server.domain.coupon;

public enum CouponType {

    /**
     * 퍼센트 할인 (예: 10% 할인)
     */
    PERCENTAGE {
        @Override
        public int applyDiscount(int price, int discountValue) {
            if (discountValue <= 0 || discountValue > 100) {
                throw new IllegalArgumentException("할인율은 1~100 사이여야 합니다.");
            }
            return price * (100 - discountValue) / 100;
        }

        @Override
        public String description() {
            return "정률 할인";
        }
    },

    /**
     * 고정 금액 할인 (예: 3,000원 할인)
     */
    FIXED {
        @Override
        public int applyDiscount(int price, int discountValue) {
            if (discountValue < 0) {
                throw new IllegalArgumentException("할인 금액은 0 이상이어야 합니다.");
            }
            return Math.max(price - discountValue, 0);
        }

        @Override
        public String description() {
            return "정액 할인";
        }
    };

    /**
     * 할인 적용 로직
     */
    public abstract int applyDiscount(int price, int discountValue);

    /**
     * UI 표시나 로깅 용 설명
     */
    public abstract String description();
}
