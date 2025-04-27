package kr.hhplus.be.server.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,  "잘못된 입력값입니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 범위입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,  "잘못된 타입의 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,  "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND,  "엔티티를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,  "서버 내부 오류가 발생했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN,  "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,  "인증이 필요합니다."),
    CONCURRENT_REQUEST(HttpStatus.CONFLICT,  "동시성 요청 충돌이 발생했습니다."),

    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,  "사용자를 찾을 수 없습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST,  "유효하지 않은 사용자 ID입니다."),

    // 상품 관련 에러
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND,  "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.UNPROCESSABLE_ENTITY,  "재고가 부족합니다."),

    // 주문 관련 에러
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND,  "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.UNPROCESSABLE_ENTITY,  "유효하지 않은 주문 상태입니다."),

    // 잔액 관련 에러
    INSUFFICIENT_BALANCE(HttpStatus.UNPROCESSABLE_ENTITY,  "잔액이 부족합니다."),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST,  "유효하지 않은 금액입니다."),
    BALANCE_UPDATE_FAILED(HttpStatus.CONFLICT,  "잔액 업데이트에 실패했습니다."),
    BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND,  "잔액 정보를 찾을 수 없습니다."),

    // 쿠폰 관련 에러
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND,  "쿠폰을 찾을 수 없습니다."),
    COUPON_EXHAUSTED(HttpStatus.UNPROCESSABLE_ENTITY,  "쿠폰이 모두 소진되었습니다."),
    COUPON_EXPIRED(HttpStatus.UNPROCESSABLE_ENTITY,  "만료된 쿠폰입니다."),
    COUPON_ALREADY_USED(HttpStatus.UNPROCESSABLE_ENTITY,  "이미 사용된 쿠폰입니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT,  "이미 발급받은 쿠폰입니다."),
    COUPON_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "쿠폰 코드가 이미 존재합니다."),
    COUPON_NOT_ISSUED(HttpStatus.UNPROCESSABLE_ENTITY, "발급되지 않은 쿠폰입니다."),

    // 외부 시스템 연동 관련 에러
    EXTERNAL_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,  "외부 시스템 연동 중 오류가 발생했습니다."),

    // 결제 관련
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 결제입니다."),
    UNSUPPORTED_PAYMENT_METHOD(HttpStatus.UNPROCESSABLE_ENTITY, "지원하지 않는 결제 수단입니다."),
    PAYMENT_PROCESSING_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "결제 처리에 실패했습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 결제 상태입니다."),

    // 인증 관련
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "인증되지 않은 접근입니다."),


    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "유효하지 않은 파라미터입니다."),

    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다.")


    ;




    private final HttpStatus status;
    private final String message;
}