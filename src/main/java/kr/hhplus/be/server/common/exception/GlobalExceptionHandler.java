package kr.hhplus.be.server.common.exception;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.common.dto.CustomApiResponse;
import kr.hhplus.be.server.common.rate.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * javax.validation.Valid 또는 @Validated 바인딩 에러 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);
        final List<String> errorDetails = createFieldErrorDetails(e.getBindingResult());
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(response);
    }

    /**
     * @ModelAttribute 바인딩 에러 처리
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleBindException(BindException e) {
        log.error("handleBindException", e);
        final List<String> errorDetails = createFieldErrorDetails(e.getBindingResult());
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(response);
    }

    /**
     * 지원하지 않는 HTTP method 호출시 발생하는 에러 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                ErrorCode.METHOD_NOT_ALLOWED.getMessage()
        );
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus()).body(response);
    }

    /**
     * 파라미터 타입 불일치 에러 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("handleMethodArgumentTypeMismatchException", e);
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                ErrorCode.INVALID_TYPE_VALUE.getMessage()
        );
        return ResponseEntity.status(ErrorCode.INVALID_TYPE_VALUE.getStatus()).body(response);
    }

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleBusinessException(BusinessException e) {
        log.error("handleBusinessException", e);
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                e.getErrorCode().getMessage()
        );
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    /**
     * 엔티티 조회 실패 예외 처리
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("handleEntityNotFoundException", e);
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                ErrorCode.ENTITY_NOT_FOUND.getMessage()
        );
        return ResponseEntity.status(ErrorCode.ENTITY_NOT_FOUND.getStatus()).body(response);
    }

    /**
     * 낙관적 락 예외 처리 (동시성 문제)
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleOptimisticLockingFailureException(OptimisticLockingFailureException e) {
        log.error("handleOptimisticLockingFailureException", e);
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                ErrorCode.CONCURRENT_REQUEST.getMessage()
        );
        return ResponseEntity.status(ErrorCode.CONCURRENT_REQUEST.getStatus()).body(response);
    }

    /**
     * 데이터 무결성 위반 예외 처리
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("handleDataIntegrityViolationException", e);
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(response);
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleException(Exception e) {
        log.error("handleException", e);
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    /**
     * 레이트 리미트 초과 예외 처리
     */
    @ExceptionHandler(RateLimitExceededException.class)
    protected ResponseEntity<CustomApiResponse<Object>> handleRateLimitExceededException(RateLimitExceededException e) {
        log.warn("handleRateLimitExceededException", e);
        final CustomApiResponse<Object> response = CustomApiResponse.error(
                ErrorCode.TOO_MANY_REQUESTS.getMessage()
        );
        return ResponseEntity.status(ErrorCode.TOO_MANY_REQUESTS.getStatus()).body(response);
    }


    private List<String> createFieldErrorDetails(BindingResult bindingResult) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return errors;
    }
}