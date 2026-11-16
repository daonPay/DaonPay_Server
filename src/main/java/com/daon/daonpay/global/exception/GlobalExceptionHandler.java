package com.daon.daonpay.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * 애플리케이션 전역 예외 처리 클래스

 * 컨트롤러 및 서비스 계층에서 발생하는 예외를 한곳에서 처리하고
 * 공통 ErrorResponse 형식으로 변환하여 클라이언트에 반환
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 사용자 정의 예외

    /**
     * 프로젝트에서 직접 정의한 CustomException 처리
     * ErrorCode에 지정된 HTTP 상태 코드와 메시지를 사용하여 응답 반환

     * 5xx 오류는 error, 4xx 오류는 warn 레벨로 로그 기록

     * @param e 발생한 사용자 정의 예외
     * @param request HTTP 요청 정보
     * @return 공통 에러 응답
     */

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            CustomException e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        if (errorCode.getStatus().is5xxServerError()) {
            log.error("CustomException 발생: {}", errorCode.getCode(), e);
        } else {
            log.warn("CustomException 발생: {} - {}", errorCode.getCode(), errorCode.getMessage());
        }

        return createResponse(errorCode, request);
    }

    // 400 BAD REQUEST - 요청 데이터 검증 오류

    /**
     * @Valid를 사용한 DTO 검증 실패 처리
     * 각 필드에서 발생한 검증 오류를 추출하여 응답에 포함

     * @param e DTO 필드 검증 실패 예외
     * @param request HTTP 요청 정보
     * @return 필드별 오류 정보가 포함된 에러 응답
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::convertFieldError)
                .toList();

        return createResponse(ErrorCode.INVALID_INPUT_VALUE, request, errors);
    }

    /**
     * 요청 데이터 바인딩 과정에서 발생한 검증 실패 처리
     * 주로 폼 데이터나 객체 필드의 바인딩 오류에 사용

     * @param e 데이터 바인딩 실패 예외
     * @param request HTTP 요청 정보
     * @return 필드별 오류 정보가 포함된 에러 응답
     */

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException e,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::convertFieldError)
                .toList();

        return createResponse(ErrorCode.INVALID_INPUT_VALUE, request, errors);
    }

    /**
     * @Validated를 사용한 메서드 파라미터 검증 실패 처리
     * 검증에 실패한 파라미터의 경로와 오류 메시지를 추출

     * @param e 파라미터 검증 실패 예외
     * @param request HTTP 요청 정보
     * @return 검증 오류 정보가 포함된 에러 응답
     */

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> errors = e.getConstraintViolations()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getPropertyPath().toString())
                        .reason(error.getMessage())
                        .build())
                .toList();

        return createResponse(ErrorCode.INVALID_INPUT_VALUE, request, errors);
    }

    /**
     * Spring 6.1 이상에서 컨트롤러 메서드 파라미터 검증 실패 처리

     * @param e 메서드 파라미터 검증 실패 예외
     * @param request HTTP 요청 정보
     * @return 검증 오류 정보가 포함된 에러 응답
     */

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException e,
            HttpServletRequest request
    ) {
        if (e.isForReturnValue()) {
            // 기존 500 에러코드(INTERNAL_SERVER_ERROR에 해당하는 것)로 응답

            log.error("컨트롤러 반환값 검증 실패 (서버 오류)", e);
        }
            // 아래는 INVALID_INPUT_VALUE(400) 처리

        List<ErrorResponse.FieldError> errors = e.getParameterValidationResults()
                .stream()
                .flatMap(result -> result.getResolvableErrors()
                        .stream()
                        .map(error -> ErrorResponse.FieldError.builder()
                                .field(result.getMethodParameter().getParameterName())
                                .reason(error.getDefaultMessage())
                                .build()))
                .toList();

        return createResponse(ErrorCode.INVALID_INPUT_VALUE, request, errors);
    }

    /**
     * 요청 파라미터의 자료형이 메서드에서 요구하는 타입과 일치하지 않을 때 처리
     * 예: 숫자를 요구하는 파라미터에 문자열이 전달된 경우

     * @param e 파라미터 타입 불일치 예외
     * @param request HTTP 요청 정보
     * @return 타입 오류 에러 응답
     */

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.INVALID_TYPE_VALUE, request);
    }

    /**
     * 필수 요청 파라미터가 전달되지 않았을 때 처리

     * @param e 필수 요청 파라미터 누락 예외
     * @param request HTTP 요청 정보
     * @return 요청 파라미터 누락 에러 응답
     */

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.MISSING_REQUEST_PARAMETER, request);
    }

    /**
     * 필수 요청 헤더가 전달되지 않았을 때 처리

     * @param e 요청 헤더 누락 예외
     * @param request HTTP 요청 정보
     * @return 요청 헤더 누락 에러 응답
     */

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeaderException(
            MissingRequestHeaderException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.MISSING_REQUEST_HEADER, request);
    }

    /**
     * JSON 요청 본문의 형식이 잘못되었거나 역직렬화에 실패했을 때 처리

     * @param e JSON 파싱 실패 예외
     * @param request HTTP 요청 정보
     * @return JSON 형식 오류 에러 응답
     */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonException(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.INVALID_JSON_FORMAT, request);
    }

    /**
     * multipart 요청에서 필수 파일 또는 데이터 파트가 누락되었을 때 처리

     * @param e 필수 요청 파트 누락 예외
     * @param request HTTP 요청 정보
     * @return 요청 파트 누락 에러 응답
     */

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPartException(
            MissingServletRequestPartException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.MISSING_REQUEST_PARAMETER, request);
    }

    /**
     * 쿠키, 세션 속성 등 요청 바인딩 과정에서 발생한 기타 오류 처리

     * @param e 요청 바인딩 실패 예외
     * @param request HTTP 요청 정보
     * @return 잘못된 요청 에러 응답
     */

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponse> handleRequestBindingException(
            ServletRequestBindingException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.INVALID_REQUEST, request);
    }

    // 401 / 403 - 인증·인가 오류

    /**
     * 인증되지 않은 사용자의 요청 처리
     * 인증 과정에서 실패한 경우 401 Unauthorized 응답 반환

     * @param e 인증 실패 예외
     * @param request HTTP 요청 정보
     * @return 인증 실패 에러 응답
     */

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.UNAUTHORIZED, request);
    }

    /**
     * 인증은 완료되었지만 요청한 리소스에 접근할 권한이 없는 경우 처리
     * 403 Forbidden 응답 반환

     * @param e 접근 권한 부족 예외
     * @param request HTTP 요청 정보
     * @return 접근 권한 없음 에러 응답
     */

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.FORBIDDEN, request);
    }

    // 404 / 405 - API 경로 및 HTTP 메서드 오류

    /**
     * 서버에서 지원하지 않는 HTTP 메서드로 요청한 경우 처리
     * 예: GET만 지원하는 API에 POST 요청을 보낸 경우

     * @param e 지원하지 않는 HTTP 메서드 예외
     * @param request HTTP 요청 정보
     * @return 405 Method Not Allowed 에러 응답
     */

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.METHOD_NOT_ALLOWED, request);
    }

    /**
     * 요청한 API 경로나 정적 리소스를 찾을 수 없는 경우 처리

     * @param e 리소스 없음 예외
     * @param request HTTP 요청 정보
     * @return 404 Not Found 에러 응답
     */

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.ENDPOINT_NOT_FOUND, request);
    }

    // 406 / 415 - 데이터 형식 관련 오류

    /**
     * 서버에서 지원하지 않는 Content-Type으로 요청한 경우 처리
     * 예: JSON만 지원하는 API에 지원하지 않는 미디어 타입을 전달한 경우

     * @param e 지원하지 않는 미디어 타입 예외
     * @param request HTTP 요청 정보
     * @return 415 Unsupported Media Type 에러 응답
     */

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeException(
            HttpMediaTypeNotSupportedException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE, request);
    }

    /**
     * 클라이언트가 요청한 Accept 타입을 서버에서 제공할 수 없는 경우 처리
     *
     * @param e 지원하지 않는 응답 미디어 타입 예외
     * @param request HTTP 요청 정보
     * @return 406 Not Acceptable 에러 응답
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleNotAcceptableException(
            HttpMediaTypeNotAcceptableException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.NOT_ACCEPTABLE, request);
    }

    // 파일 업로드 관련 오류

    /**
     * 업로드 파일이 서버에서 설정한 최대 허용 용량을 초과한 경우 처리

     * @param e 파일 용량 초과 예외
     * @param request HTTP 요청 정보
     * @return 파일 용량 초과 에러 응답
     */

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeException(
            MaxUploadSizeExceededException e,
            HttpServletRequest request
    ) {
        return createResponse(ErrorCode.FILE_SIZE_EXCEEDED, request);
    }

    /**
     * multipart 파일 업로드 요청을 처리하는 과정에서 발생한 오류 처리

     * @param e multipart 요청 처리 예외
     * @param request HTTP 요청 정보
     * @return 파일 업로드 실패 에러 응답
     */

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(
            MultipartException e,
            HttpServletRequest request
    ) {
        log.warn("파일 업로드 요청 오류", e);

        return createResponse(ErrorCode.FILE_UPLOAD_ERROR, request);
    }

    // 데이터베이스 관련 오류

    /**
     * 데이터베이스 동시성 제어 과정에서 발생한 충돌 처리

     * 낙관적 락 충돌 또는 데이터베이스 락 획득 실패 시 발생
     * 409 Conflict 응답 반환

     * @param e 동시성 충돌 예외
     * @param request HTTP 요청 정보
     * @return 동시성 충돌 에러 응답
     */

    @ExceptionHandler({
            OptimisticLockingFailureException.class,
            CannotAcquireLockException.class
    })
    public ResponseEntity<ErrorResponse> handleConcurrencyException(
            Exception e,
            HttpServletRequest request
    ) {
        log.warn("동시성 충돌 발생", e);

        return createResponse(ErrorCode.CONCURRENT_UPDATE, request);
    }

    /**
     * 데이터베이스의 무결성 제약조건을 위반한 경우 처리
     * 예: UNIQUE, FOREIGN KEY 제약조건 위반

     * @param e 데이터 무결성 제약조건 위반 예외
     * @param request HTTP 요청 정보
     * @return 데이터 충돌 에러 응답
     */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityException(
            DataIntegrityViolationException e,
            HttpServletRequest request
    ) {
        log.warn("데이터 무결성 제약조건 위반", e);

        return createResponse(ErrorCode.DATA_CONFLICT, request);
    }

    /**
     * 데이터베이스에 접근하거나 쿼리를 실행하는 과정에서 발생한 오류 처리

     * @param e 데이터베이스 접근 예외
     * @param request HTTP 요청 정보
     * @return 데이터베이스 오류 에러 응답
     */

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(
            DataAccessException e,
            HttpServletRequest request
    ) {
        log.error("데이터베이스 접근 오류", e);

        return createResponse(ErrorCode.DATABASE_ERROR, request);
    }

    // 외부 API 통신 오류

    /**
     * 외부 서비스와 통신하는 과정에서 연결 실패 또는 시간 초과가 발생한 경우 처리

     * @param e 외부 서비스 연결 예외
     * @param request HTTP 요청 정보
     * @return 외부 API 연결 오류 에러 응답
     */

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiAccess(
            ResourceAccessException e,
            HttpServletRequest request
    ) {
        if (isTimeout(e)) {
            log.error("외부 서비스 응답 시간 초과", e);
            return createResponse(ErrorCode.EXTERNAL_API_TIMEOUT, request);
        }

        log.error("외부 서비스 연결 실패", e);
        return createResponse(ErrorCode.EXTERNAL_API_CONNECTION_FAILED, request);
    }

    private boolean isTimeout(Throwable t) {
        for (int depth = 0; t != null && depth < 10; depth++, t = t.getCause()) {
            if (t instanceof java.net.SocketTimeoutException
                    || t instanceof java.net.http.HttpTimeoutException
                    || t instanceof java.io.InterruptedIOException) {
                return true;
            }
        }
        return false;
    }

    /**
     * 외부 API 요청 및 응답 처리 과정에서 발생한 일반적인 통신 오류 처리

     * @param e 외부 API 통신 예외
     * @param request HTTP 요청 정보
     * @return 외부 API 통신 오류 에러 응답
     */

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            RestClientException e,
            HttpServletRequest request
    ) {
        log.error("외부 API 통신 오류", e);

        return createResponse(ErrorCode.EXTERNAL_API_ERROR, request);
    }

    //  500 INTERNAL SERVER ERROR - 기타 서버 오류

    /**
     * 위에서 별도로 처리하지 않은 모든 예외 처리
     * 예상하지 못한 서버 내부 오류에 대해 500 응답 반환

     * @param e 발생한 예외
     * @param request HTTP 요청 정보
     * @return 내부 서버 오류 에러 응답
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("예상하지 못한 서버 오류", e);

        return createResponse(ErrorCode.INTERNAL_SERVER_ERROR, request);
    }

    // 공통 메서드

    /**
     * Spring에서 제공하는 FieldError 객체를
     * 프로젝트의 ErrorResponse.FieldError 객체로 변환

     * @param error Spring에서 발생한 필드 검증 오류
     * @return 변환된 필드 오류 정보
     */

    private ErrorResponse.FieldError convertFieldError(FieldError error) {
        return ErrorResponse.FieldError.builder()
                .field(error.getField())
                .reason(error.getDefaultMessage())
                .build();
    }

    /**
     * 일반적인 예외 응답 생성
     * ErrorCode에 지정된 HTTP 상태 코드와 요청 경로를 포함하여 응답 반환

     * @param errorCode 응답에 사용할 에러 코드
     * @param request HTTP 요청 정보
     * @return 공통 에러 응답
     */

    private ResponseEntity<ErrorResponse> createResponse(
            ErrorCode errorCode,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI()));
    }

    /**
     * 필드 검증 오류가 포함된 예외 응답 생성
     * 여러 필드에서 발생한 검증 오류를 응답 본문에 함께 포함

     * @param errorCode 응답에 사용할 에러 코드
     * @param request HTTP 요청 정보
     * @param errors 필드별 검증 오류 목록
     * @return 필드 오류 정보가 포함된 공통 에러 응답
     */

    private ResponseEntity<ErrorResponse> createResponse(
            ErrorCode errorCode,
            HttpServletRequest request,
            List<ErrorResponse.FieldError> errors
    ) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI(), errors));
    }
}