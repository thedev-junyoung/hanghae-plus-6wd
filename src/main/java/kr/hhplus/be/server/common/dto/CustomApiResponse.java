package kr.hhplus.be.server.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 응답")
public class CustomApiResponse<T> {

    @Schema(description = "응답 상태", example = "success", allowableValues = {"success", "error"})
    private final String status;

    @Schema(description = "응답 데이터 (성공 시)")
    private final T data;

    @Schema(description = "에러 메시지 (실패 시)")
    private final String message;

    private CustomApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    /**
     * 성공 응답 생성
     */
    public static <T> CustomApiResponse<T> success(T data) {
        return new CustomApiResponse<>("success", data, null);
    }

    /**
     * 에러 응답 생성
     */
    public static <T> CustomApiResponse<T> error(String message) {
        return new CustomApiResponse<>("error", null,message);
    }
}