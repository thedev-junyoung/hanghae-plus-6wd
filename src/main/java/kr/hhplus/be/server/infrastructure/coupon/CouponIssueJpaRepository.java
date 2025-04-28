package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponIssueJpaRepository extends JpaRepository<CouponIssue, Long> {
    boolean existsByUserIdAndCoupon_Id(Long userId, Long couponId);
    Optional<CouponIssue> findByUserIdAndCoupon_Id(Long userId, Long couponId);

    @Query("SELECT COUNT(ci) FROM CouponIssue ci WHERE ci.coupon.code = :code")
    long countByCouponCode(@Param("code") String code);

    @Query("SELECT ci FROM CouponIssue ci JOIN FETCH ci.coupon WHERE ci.userId = :userId AND ci.coupon.id = :couponId")
    Optional<CouponIssue> findByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);

}
