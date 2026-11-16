package com.daon.daonpay.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// API 요청 처리 중 발생한 예외 정보를 담는 응답 DTO
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    // 에러 식별 코드
    private final String code;

    // 에러 상세 메시지
    private final String message;

    // 에러가 발생한 요청 경로
    private final String path;

    // 에러 발생 시간
    private final LocalDateTime timestamp;

    // 입력값 검증 실패 시 발생한 필드별 오류 목록
    private final List<FieldError> errors;


    // 필드별 검증 오류 정보를 담는 내부 클래스
    @Getter
    @Builder
    public static class FieldError {

        // 오류가 발생한 필드명
        private final String field;

        // 해당 필드의 오류 원인
        private final String reason;
    }


    // 일반적인 예외 응답 생성
    // 필드별 검증 오류가 없는 경우 사용
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }


    // 필드별 검증 오류가 포함된 예외 응답 생성
    // 입력값 검증 실패 등의 경우 사용
    public static ErrorResponse of(
            ErrorCode errorCode,
            String path,
            List<FieldError> errors
    ) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }
}
