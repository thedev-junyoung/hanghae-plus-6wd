package kr.hhplus.be.server.interfaces.coupon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import kr.hhplus.be.server.common.exception.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Coupon", description = "쿠폰 발급 및 조회 API")
@RequestMapping("/api/v1/coupons")
public interface CouponAPI {

    @Operation(
            summary = "한정 수량 쿠폰 발급",
            description = """
            한정 수량 쿠폰을 사용자가 발급받는 API입니다.
            
            - 한정 수량 초과 시 `422 UNPROCESSABLE_ENTITY` 반환
            - 이미 발급받은 사용자는 `409 CONFLICT` 반환
            - 쿠폰이 만료되었거나 존재하지 않으면 `404 NOT_FOUND` 반환
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "발급받을 쿠폰 정보",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "쿠폰 발급 성공",
                            content = @Content(schema = @Schema(implementation = CouponResponse.class))),
                    @ApiResponse(responseCode = "404", description = "쿠폰이 존재하지 않거나 만료됨",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "이미 발급받은 쿠폰",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "422", description = "발급 가능한 수량 초과",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    @PostMapping("/limited-issue")
    ResponseEntity<CustomApiResponse<CouponResponse>> limitedIssueCoupon(
            @Valid @RequestBody CouponRequest request
    );

}

