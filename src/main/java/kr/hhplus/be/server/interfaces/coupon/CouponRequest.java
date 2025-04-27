package kr.hhplus.be.server.interfaces.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.application.coupon.IssueLimitedCouponCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest {

    @NotNull
    @Schema(description = "사용자 ID", example = "1001")
    private Long userId;

    @NotBlank
    @Schema(description = "쿠폰 코드", example = "LIMITED2025")
    private String couponCode;

    public IssueLimitedCouponCommand toCommand() {
        return new IssueLimitedCouponCommand(userId, couponCode);
    }
}
