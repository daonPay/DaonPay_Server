package global.jwt;

import global.exception.CustomException;
import global.exception.ErrorCode;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 기반 사용자 인증을 처리하는 필터

 * 요청 헤더에서 JWT를 추출하고 유효성을 검증한 후
 * 인증 정보를 SecurityContext에 등록

 * @Component를 사용하지 않고 SecurityConfig에서 직접 생성하여 등록
 */

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 생성 및 검증, 인증 객체 변환을 담당하는 클래스
    private final JwtProvider jwtProvider;

    /**
     * HTTP 요청마다 JWT 인증 처리 수행

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

        // Authorization 헤더에서 JWT 추출
        String token = resolveToken(request);


        // 토큰이 없는 경우 인증 정보를 등록하지 않고 다음 필터로 진행
        // 인증이 필요한 경로라면 이후 Security의 AuthenticationEntryPoint에서 401 처리

        if (token != null) {
            try {

                // JWT 검증 후 사용자 인증 객체를 생성하여 SecurityContext에 등록
                // 이후 컨트롤러와 서비스에서 인증된 사용자 정보에 접근 가능

                SecurityContextHolder.getContext()
                        .setAuthentication(jwtProvider.getAuthentication(token));

            } catch (ExpiredJwtException e) {

                // 토큰의 유효 기간이 만료된 경우 TOKEN_EXPIRED 예외 발생
                throw new CustomException(ErrorCode.TOKEN_EXPIRED);

            } catch (JwtException | IllegalArgumentException e) {

                // 토큰의 서명이 잘못되었거나 형식이 올바르지 않은 경우 INVALID_TOKEN 예외 발생
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }
        }

        // JWT 인증 처리 후 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 추출

     * Authorization 헤더의 Bearer 인증 방식을 사용

     * @param request HTTP 요청 객체
     * @return 추출한 JWT 문자열 또는 토큰이 없는 경우 null
     */

    private String resolveToken(HttpServletRequest request) {

        // Authorization 헤더 값 조회
        String header = request.getHeader("Authorization");

        // Bearer 인증 형식인지 확인
        if (header != null && header.startsWith("Bearer ")) {

            // "Bearer " 접두사를 제외한 실제 JWT 문자열 반환
            return header.substring(7);
        }

        // Authorization 헤더가 없거나 Bearer 형식이 아닌 경우 null 반환
        return null;
    }
}