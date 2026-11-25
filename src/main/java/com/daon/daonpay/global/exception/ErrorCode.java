
package com.daon.daonpay.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 BAD REQUEST - 잘못된 요청

    INVALID_INPUT_VALUE(
            HttpStatus.BAD_REQUEST,
            "COMMON_001",
            "잘못된 입력값입니다."
    ),

    INVALID_TYPE_VALUE(
            HttpStatus.BAD_REQUEST,
            "COMMON_002",
            "요청 데이터의 타입이 올바르지 않습니다."
    ),

    MISSING_REQUEST_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "COMMON_003",
            "필수 요청 파라미터가 누락되었습니다."
    ),

    INVALID_JSON_FORMAT(
            HttpStatus.BAD_REQUEST,
            "COMMON_004",
            "요청 JSON 형식이 올바르지 않습니다."
    ),

    MISSING_REQUEST_HEADER(
            HttpStatus.BAD_REQUEST,
            "COMMON_005",
            "필수 요청 헤더가 누락되었습니다."
    ),

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "COMMON_006",
            "올바르지 않은 요청입니다."
    ),

    // 401 UNAUTHORIZED - 인증되지 않은 사용자

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_001",
            "인증이 필요합니다."
    ),

    // 401 INVALID - 유효하지 않은 토큰
    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_002",
            "유효하지 않은 토큰입니다."
    ),

    // 401 TOKEN_EXPIRED - 만료된 토큰
    TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_003",
            "만료된 토큰입니다."
    ),

    // 403 FORBIDDEN - 권한 없음

    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "AUTH_004",
            "접근 권한이 없습니다."
    ),

    // 404 NOT FOUND - 리소스를 찾을 수 없음

    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COMMON_007",
            "요청한 리소스를 찾을 수 없습니다."
    ),

    ENDPOINT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COMMON_008",
            "요청한 API를 찾을 수 없습니다."
    ),


    // 405 METHOD NOT ALLOWED - 지원하지 않는 메서드

    METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED,
            "COMMON_009",
            "지원하지 않는 HTTP 메서드입니다."
    ),

    // 406 NOT ACCEPTABLE - 지원하지 않는 응답 형식

    NOT_ACCEPTABLE(
            HttpStatus.NOT_ACCEPTABLE,
            "COMMON_010",
            "지원하지 않는 응답 형식입니다."
    ),


    // 409 CONFLICT - 데이터 충돌

    DUPLICATE_RESOURCE(
            HttpStatus.CONFLICT,
            "COMMON_011",
            "이미 존재하는 데이터입니다."
    ),

    DATA_CONFLICT(
            HttpStatus.CONFLICT,
            "COMMON_012",
            "데이터 처리 중 충돌이 발생했습니다."
    ),

    CONCURRENT_UPDATE(
            HttpStatus.CONFLICT,
            "COMMON_018",
            "다른 요청과 동시에 처리되어 실패했습니다. 잠시 후 다시 시도해주세요."
    ),


    // 413 PAYLOAD TOO LARGE - 요청 데이터 크기 초과

    PAYLOAD_TOO_LARGE(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "COMMON_013",
            "요청 데이터의 크기가 너무 큽니다."
    ),

    FILE_SIZE_EXCEEDED(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "COMMON_014",
            "파일 용량 제한을 초과했습니다."
    ),

    // 415 UNSUPPORTED MEDIA TYPE - 지원하지 않는 데이터 형식

    UNSUPPORTED_MEDIA_TYPE(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "COMMON_015",
            "지원하지 않는 데이터 형식입니다."
    ),

    INVALID_FILE_FORMAT(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "COMMON_016",
            "지원하지 않는 파일 형식입니다."
    ),


    // 429 TOO MANY REQUESTS - 요청 횟수 초과

    TOO_MANY_REQUESTS(
            HttpStatus.TOO_MANY_REQUESTS,
            "COMMON_017",
            "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해 주세요."
    ),

    // 500 INTERNAL SERVER ERROR - 서버 내부 오류

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SERVER_001",
            "서버 내부 오류가 발생했습니다."
    ),

    DATABASE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SERVER_002",
            "데이터 처리 중 오류가 발생했습니다."
    ),

    FILE_UPLOAD_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SERVER_003",
            "파일 처리 중 오류가 발생했습니다."
    ),

    // 502 BAD GATEWAY - 외부 서비스 통신 오류

    EXTERNAL_API_ERROR(
            HttpStatus.BAD_GATEWAY,
            "SERVER_004",
            "외부 서비스와 통신하는 중 오류가 발생했습니다."
    ),

    // 503 SERVICE UNAVAILABLE - 서비스 이용 불가

    SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "SERVER_005",
            "현재 서비스를 이용할 수 없습니다."
    ),

    // 504 GATEWAY TIMEOUT - 외부 서비스 응답 시간 초과

    EXTERNAL_API_TIMEOUT(
            HttpStatus.GATEWAY_TIMEOUT,
            "SERVER_006",
            "외부 서비스 응답 시간이 초과되었습니다."
    ),
    EXTERNAL_API_CONNECTION_FAILED(
            HttpStatus.BAD_GATEWAY,
            "SERVER_007",
            "외부 서비스에 연결할 수 없습니다."
    ),

    // 결제

    PAYMENT_RESULT_UNKNOWN(
            HttpStatus.GATEWAY_TIMEOUT,
            "PAYMENT_001",
            "결제 결과를 확인하고 있습니다. 거래 내역을 확인한 뒤 다시 시도해 주세요."
    ),
    PAYMENT_IN_PROGRESS(
            HttpStatus.CONFLICT,
            "PAYMENT_002",
            "이미 처리 중인 결제입니다."
    ),
    IDEMPOTENCY_KEY_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "PAYMENT_003",
            "결제 요청 식별키가 필요합니다."
    ),
    IDEMPOTENCY_KEY_REUSED(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "PAYMENT_004",
            "같은 식별키로 다른 내용의 결제를 요청할 수 없습니다."
    ),

    SUPPORT_FUND_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SUPPORT_FUND_001",
            "재난지원금 지갑을 찾을 수 없습니다."
    ),

    INVALID_SUPPORT_FUND_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "SUPPORT_FUND_002",
            "재난지원금 금액은 0보다 커야 합니다."
    ),

    // place 예외 처리
    // 404 NOT FOUND - 사용처를 찾을 수 없음

    PLACE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PLACE_001",
            "사용처를 찾을 수 없습니다."
    ),

// 409 CONFLICT - 운영 중단 등으로 이용할 수 없는 사용처

    PLACE_UNAVAILABLE(
            HttpStatus.CONFLICT,
            "PLACE_002",
            "현재 이용할 수 없는 사용처입니다."
    ),

// 404 NOT FOUND - 상품을 찾을 수 없음

    PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PRODUCT_001",
            "상품을 찾을 수 없습니다."
    ),

// 409 CONFLICT - 같은 사용처에 이미 등록된 상품

    PRODUCT_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "PRODUCT_002",
            "이미 등록된 상품입니다."
    ),

    WHITELIST_PROPOSAL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "WHITELIST_PROPOSAL_001",
            "화이트리스트 변경 제안을 찾을 수 없습니다."
    ),

    WHITELIST_PROPOSAL_ALREADY_REVIEWED(
            HttpStatus.CONFLICT,
            "WHITELIST_PROPOSAL_002",
            "이미 검토가 끝난 화이트리스트 변경 제안입니다."
    ),

    // user

    INVALID_ROLE(
            HttpStatus.BAD_REQUEST,
            "INVALID_ROLE",
            "선택할 수 없는 역할입니다."),

    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "DUPLICATE_EMAIL",
            "이미 사용 중인 이메일입니다."),

    LOGIN_FAILED(
            HttpStatus.UNAUTHORIZED,
            "LOGIN_FAILED",
            "이메일 또는 비밀번호가 올바르지 않습니다.");

    // ERROR CODE FIELD

    private final HttpStatus status;
    private final String code;
    private final String message;
}
