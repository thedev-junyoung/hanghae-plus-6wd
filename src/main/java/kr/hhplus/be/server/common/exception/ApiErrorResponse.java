package kr.hhplus.be.server.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 에러 응답")
public class ApiErrorResponse {

    @Schema(description = "응답 상태", example = "error")
    private final String status = "error";

    @Schema(description = "에러 메시지", example = "상품을 찾을 수 없습니다.")
    private final String message;

    @Schema(description = "에러 발생 시간", example = "2025-04-02T10:15:30.123")
    private final LocalDateTime timestamp;

    @Schema(description = "추가 에러 정보")
    private final Object details;

    private ApiErrorResponse(String message, Object details) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    public static ApiErrorResponse of(ErrorCode errorCode) {
        return new ApiErrorResponse( errorCode.getMessage(), null);
    }

    public static ApiErrorResponse of(ErrorCode errorCode, Object details) {
        return new ApiErrorResponse(errorCode.getMessage(), details);
    }

    public static ApiErrorResponse of(String message) {
        return new ApiErrorResponse(message, null);
    }

    public static ApiErrorResponse of(String message, Object details) {
        return new ApiErrorResponse(message, details);
    }
}