
package com.daon.daonpay.global.jwt;

import com.daon.daonpay.global.jwt.JwtProvider;
import com.daon.daonpay.global.util.ErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 기반 사용자 인증을 처리하는 필터
 * HTTP 요청 헤더에서 JWT를 추출하고 토큰을 검증한 후
 * 인증된 사용자 정보를 Spring Security의 SecurityContext에 등록
 */

// 주의: @Component 로 등록하지 않는다.
// Bean 으로 등록하면 서블릿 컨테이너에도 자동 등록되어 Security 필터 체인 밖에서 한 번 더 실행
// SecurityConfig.filterChain 안에서 직접 생성하여 Security 체인에서만 동작하게 함

// final 필드를 대상으로 생성자를 자동 생성 (SecurityConfig 에서 new JwtAuthenticationFilter(jwtProvider) 로 생성)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Authorization 헤더에서 사용하는 Bearer 인증 접두사
    private static final String BEARER_PREFIX = "Bearer ";

    // JWT 검증 및 인증된 사용자 정보 추출을 담당하는 클래스
    private final JwtProvider jwtProvider;



    /**
     * HTTP 요청마다 JWT 인증 처리 수행

     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param chain 다음 필터로 요청을 전달하는 필터 체인
     * @throws ServletException 서블릿 처리 중 발생하는 예외
     * @throws IOException 입출력 처리 중 발생하는 예외
     */

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        // Authorization 헤더에서 JWT 추출
        String token = resolveToken(request);

        // 토큰이 존재하는 경우에만 사용자 인증 처리
        // 토큰이 없는 경우 인증 정보를 등록하지 않고 다음 필터로 진행
        // 인증이 필요한 경로라면 이후 Spring Security에서 인증 실패 처리
        if (token != null) {

            // JWT 검증 후 사용자 ID와 역할 정보를 AuthUser 객체로 추출
            // 토큰이 만료되거나 유효하지 않으면 CustomException 발생
            // 발생한 예외는 JwtExceptionFilter에서 공통 에러 응답으로 변환
            AuthUser authUser = jwtProvider.getAuthUser(token);

            // 추출한 사용자 정보를 Spring Security의 Authentication 객체로 변환
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(

                            // principal: 인증된 사용자의 ID와 역할 정보를 담은 객체
                            authUser,

                            // credentials: 별도의 비밀번호 정보는 사용하지 않음
                            null,

                            // authorities: 사용자의 역할을 Spring Security 권한 객체로 변환
                            // 이후 권한 기반 접근 제어에 사용
                            List.of(new SimpleGrantedAuthority(authUser.role().authority()))
                    );

            // 생성한 인증 객체를 SecurityContext에 등록
            // 이후 컨트롤러에서 @AuthenticationPrincipal로 사용자 정보 접근 가능
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // JWT 인증 처리 후 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 추출
     * Authorization 헤더의 Bearer 인증 방식을 사용

     * @param request HTTP 요청 객체
     * @return 추출한 JWT 문자열 또는 토큰이 없는 경우 null
     */

    private String resolveToken(HttpServletRequest request) {

        // Authorization 헤더 값 조회
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 헤더가 존재하고 Bearer 인증 형식인지 확인
        if (header != null && header.startsWith(BEARER_PREFIX)) {

            // "Bearer " 접두사를 제외한 실제 JWT 문자열 반환
            return header.substring(BEARER_PREFIX.length());
        }

        // Authorization 헤더가 없거나 Bearer 형식이 아닌 경우 null 반환
        return null;
    }
}