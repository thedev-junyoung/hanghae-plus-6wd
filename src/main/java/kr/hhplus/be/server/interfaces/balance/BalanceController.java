package kr.hhplus.be.server.interfaces.balance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.balance.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kr.hhplus.be.server.common.dto.CustomApiResponse;

@RestController
@RequestMapping("/api/v1/balances")
@RequiredArgsConstructor
@Tag(name = "Balance", description = "잔액 관리 API")
public class BalanceController implements BalanceAPI{

    private final BalanceUseCase balanceUseCase;
    private final BalanceFacade balanceFacade;

    @PostMapping("/charge")
    public ResponseEntity<CustomApiResponse<BalanceResponse>> charge(@Valid @RequestBody BalanceRequest request) {
        ChargeBalanceCriteria criteria = ChargeBalanceCriteria.fromRequest(request);

        BalanceResult result = balanceFacade.charge(criteria);

        return ResponseEntity.ok(CustomApiResponse.success(
                new BalanceResponse(result.userId(), result.balance(), result.updatedAt())
        ));
    }



    @GetMapping("/{userId}")
    @Operation(summary = "잔액 조회", description = "사용자의 현재 잔액을 조회합니다.")
    public ResponseEntity<CustomApiResponse<BalanceResponse>> getBalance(@PathVariable Long userId) {
        BalanceResult result = balanceUseCase.getBalance(userId);
        return ResponseEntity.ok(CustomApiResponse.success(
                new BalanceResponse(result.userId(), result.balance(), result.updatedAt())
        ));
    }
}