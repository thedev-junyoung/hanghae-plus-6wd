package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;


/**
 * 쿠폰 발급 엔티티 ( == UserCoupon)
 */
@Entity
@Table(name = "coupon_issue")
@Getter
@NoArgsConstructor
public class CouponIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private boolean isUsed = false;

    // 생성자
    public CouponIssue(Long userId, Coupon coupon, Clock clock) {
        this.userId = userId;
        this.coupon = coupon;
        this.issuedAt = LocalDateTime.now(clock);
    }

    public static CouponIssue create(Long userId, Coupon coupon, Clock clock) {
        coupon.validateUsable(clock);
        coupon.decreaseQuantity(clock);
        return new CouponIssue(userId, coupon, clock);
    }



    public void markAsUsed() {
        if (this.isUsed) {
            throw new CouponException.AlreadyIssuedException(userId, coupon.getCode());
        }
        this.isUsed = true;
    }


}
