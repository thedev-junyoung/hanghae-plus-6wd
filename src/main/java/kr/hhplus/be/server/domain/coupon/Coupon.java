package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.vo.Money;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(nullable = false)
    private Integer discountRate;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer remainingQuantity;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    public Coupon(String code, CouponType type, Integer discountRate, Integer totalQuantity,
                  Integer remainingQuantity, LocalDateTime validFrom, LocalDateTime validUntil) {
        this.code = code;
        this.type = type;
        this.discountRate = discountRate;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = remainingQuantity;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public static Coupon create(String code, CouponType type, Integer discountRate, Integer totalQuantity,
                                 LocalDateTime validFrom, LocalDateTime validUntil) {
        return new Coupon(code, type, discountRate, totalQuantity, totalQuantity, validFrom, validUntil);
    }

    public static Coupon createLimitedFixed(String code, int discountAmount, int quantity,
                                            LocalDateTime validFrom, LocalDateTime validUntil) {
        return new Coupon(
                code,
                CouponType.FIXED,
                discountAmount,
                quantity,
                quantity,
                validFrom,
                validUntil
        );
    }

    public static Coupon createLimitedPercentage(String code, int discountRate, int quantity,
                                                 LocalDateTime validFrom, LocalDateTime validUntil) {
        return new Coupon(
                code,
                CouponType.PERCENTAGE,
                discountRate,
                quantity,
                quantity,
                validFrom,
                validUntil
        );
    }
    public boolean isExpired(Clock clock) {
        return Policy.isExpired(validUntil, clock);
    }

    public boolean isExhausted() {
        return Policy.isExhausted(remainingQuantity);
    }

    /**
     * 쿠폰 정책 자체의 유효성을 검증
     * - 유효 기간이 지났는지 확인
     * - 발급 가능한 수량이 남아있는지 확인
     * 해당 검증은 쿠폰이 발급되거나 사용할 수 있는 상태인지 판단하는
     * "정책 관점"의 검증
     */
    public void validateUsable(Clock clock) {
        if (isExpired(clock)) {
            throw new CouponException.ExpiredException();
        }
        if (isExhausted()) {
            throw new CouponException.AlreadyExhaustedException();
        }
    }

    public void decreaseQuantity(Clock clock) {
        validateUsable(clock);
        this.remainingQuantity -= 1;
    }

    public Money calculateDiscount(Money orderAmount) {
        return switch (this.type) {
            case FIXED -> Money.wons(this.discountRate);
            case PERCENTAGE -> orderAmount.multiplyPercent(this.discountRate);
        };
    }


    static class Policy {
        public static boolean isExpired(LocalDateTime until, Clock clock) {
            return LocalDateTime.now(clock).isAfter(until);
        }

        public static boolean isExhausted(int quantity) {
            return quantity <= 0;
        }
    }


}
