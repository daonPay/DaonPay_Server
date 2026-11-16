package com.daon.daonpay.global.exception;

import com.daon.daonpay.global.util.ErrorResponseWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 인증되지 않은 사용자의 접근 시 발생하는 예외를 처리하는 클래스
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        // 인증 실패 시 401 응답 반환
        errorResponseWriter.write(
                request,
                response,
                ErrorCode.UNAUTHORIZED
        );
    }
}