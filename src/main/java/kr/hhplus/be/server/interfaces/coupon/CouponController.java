package kr.hhplus.be.server.interfaces.coupon;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.coupon.CouponResult;
import kr.hhplus.be.server.application.coupon.CouponUseCase;
import kr.hhplus.be.server.application.coupon.IssueLimitedCouponCommand;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupon", description = "쿠폰 API")
public class CouponController implements CouponAPI {

    private final CouponUseCase couponUseCase;

    @Override
    public ResponseEntity<CustomApiResponse<CouponResponse>> limitedIssueCoupon(@Valid @RequestBody CouponRequest request) {
        IssueLimitedCouponCommand command = request.toCommand();

        CouponResult result = couponUseCase.issueLimitedCoupon(command);

        return ResponseEntity.ok(CustomApiResponse.success(
                new CouponResponse(
                        result.userCouponId(),
                        result.userId(),
                        result.couponType(),
                        result.discountRate(),
                        result.issuedAt(),
                        result.expiryDate()
                )
        ));
    }
}
