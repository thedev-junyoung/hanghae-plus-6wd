package kr.hhplus.be.server.interfaces.balance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.balance.BalanceResult;
import kr.hhplus.be.server.common.exception.ApiErrorResponse;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Balance", description = "잔액 관리 API")
@RequestMapping("/api/v1/balances")
public interface BalanceAPI {

    @Operation(summary = "잔액 충전", description = "사용자의 잔액을 충전합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = BalanceResult.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/charge")
    ResponseEntity<CustomApiResponse<BalanceResponse>> charge(
            @Parameter(description = "잔액 충전 요청", required = true)
            @Valid @RequestBody BalanceRequest request
    );

    @Operation(summary = "잔액 조회", description = "사용자의 현재 잔액을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = BalanceResult.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{userId}")
    ResponseEntity<CustomApiResponse<BalanceResponse>> getBalance(
            @Parameter(description = "사용자 ID", example = "12345", required = true)
            @PathVariable Long userId
    );
}