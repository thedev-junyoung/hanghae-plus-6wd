package kr.hhplus.be.server.interfaces.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponResponse {

    @Schema(description = "발급된 사용자 쿠폰 ID", example = "2001")
    private final Long userCouponId;

    @Schema(description = "사용자 ID", example = "1001")
    private final Long userId;

    @Schema(description = "쿠폰 타입", example = "LIMITED")
    private final String couponType;

    @Schema(description = "할인율", example = "20")
    private final Integer discountRate;

    @Schema(description = "발급 시각", example = "2025-04-10T10:00:00")
    private final LocalDateTime issuedAt;

    @Schema(description = "만료 시각", example = "2025-04-30T23:59:59")
    private final LocalDateTime expiryDate;

    public CouponResponse(Long userCouponId, Long userId, String couponType, Integer discountRate,
                          LocalDateTime issuedAt, LocalDateTime expiryDate) {
        this.userCouponId = userCouponId;
        this.userId = userId;
        this.couponType = couponType;
        this.discountRate = discountRate;
        this.issuedAt = issuedAt;
        this.expiryDate = expiryDate;
    }
}
