
package com.daon.daonpay.global.exception;

import com.daon.daonpay.global.util.ErrorResponseWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 인증은 완료되었지만 요청한 리소스에 접근할 권한이 없는 경우
// 403 Forbidden 응답을 반환하는 핸들러
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // 공통 에러 응답을 작성하는 유틸리티 클래스
    private final ErrorResponseWriter errorResponseWriter;

    // 접근 권한이 없는 요청이 발생했을 때 실행되는 메서드
    @Override
    public void handle(
            HttpServletRequest request, // 클라이언트의 요청 정보
            HttpServletResponse response, // 클라이언트에게 반환할 응답 정보
            AccessDeniedException accessDeniedException // 접근 거부 예외 정보
    ) throws IOException, ServletException {

        // 공통 에러 응답 작성기를 통해 FORBIDDEN 에러 응답 반환
        errorResponseWriter.write(request, response, ErrorCode.FORBIDDEN);
    }
}
