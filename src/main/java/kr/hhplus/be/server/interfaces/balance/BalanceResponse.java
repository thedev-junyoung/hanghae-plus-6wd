package kr.hhplus.be.server.interfaces.balance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BalanceResponse {

    @Schema(description = "사용자 ID", example = "12345")
    private final Long userId;

    @Schema(description = "현재 잔액", example = "150000")
    private final long balance;

    @Schema(description = "잔액 갱신 시각", example = "2025-04-02T10:30:00")
    private final LocalDateTime updatedAt;

    public BalanceResponse(Long userId, long balance, LocalDateTime updatedAt) {
        this.userId = userId;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }
}
