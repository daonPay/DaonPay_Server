package global.exception;

import global.util.ErrorResponseWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터에서 발생한 CustomException을 처리하는 필터
 * 예외 발생 시 공통 에러 응답 형식의 JSON으로 변환하여 반환
 */

@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    // 예외에 해당하는 에러 정보를 HTTP 응답으로 작성하는 유틸리티
    private final ErrorResponseWriter errorResponseWriter;

    /**
     * 요청마다 한 번 실행되는 JWT 예외 처리 필터

     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 다음 필터로 요청을 전달하는 필터 체인
     * @throws ServletException 서블릿 처리 중 발생하는 예외
     * @throws IOException 입출력 처리 중 발생하는 예외
     */

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 다음 필터로 요청을 전달하고 이후 필터에서 발생하는 예외 감지
            filterChain.doFilter(request, response);

        } catch (CustomException e) {

            // 발생한 CustomException에서 ErrorCode를 추출하여 JSON 에러 응답 반환
            errorResponseWriter.write(request, response, e.getErrorCode());
        }
    }
}