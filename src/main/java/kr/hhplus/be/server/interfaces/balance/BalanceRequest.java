package kr.hhplus.be.server.interfaces.balance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;


public record BalanceRequest(@Schema(description = "사용자 ID", example = "12345") @NotNull Long userId,
                             @Schema(description = "충전 금액", example = "50000") @NotNull @Min(0) long amount,
                             @Schema(description = "고유 요청 식별자", example = "request-12345") @NotNull String requestId) {

    public BalanceRequest(Long userId, long amount, String requestId) {
        this.userId = userId;
        this.amount = amount;
        this.requestId = requestId;
    }
}
