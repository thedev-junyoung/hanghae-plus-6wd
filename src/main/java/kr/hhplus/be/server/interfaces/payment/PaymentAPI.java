package kr.hhplus.be.server.interfaces.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "결제 API")
@RequestMapping("/api/v1/payments")
public interface PaymentAPI {

    @Operation(summary = "결제 요청", description = "주문에 대한 결제를 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "422", description = "잔액 부족",
                    content = @Content(schema = @Schema(implementation = kr.hhplus.be.server.common.exception.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = kr.hhplus.be.server.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping
    ResponseEntity<CustomApiResponse<PaymentResponse>> requestPayment(
            @Valid @RequestBody PaymentRequest request
    );

}
