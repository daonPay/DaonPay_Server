package com.daon.daonpay.global.config;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.global.exception.CustomAccessDeniedHandler;
import com.daon.daonpay.global.exception.CustomAuthenticationEntryPoint;
import com.daon.daonpay.global.exception.JwtExceptionFilter;
import com.daon.daonpay.global.jwt.JwtAuthenticationFilter;
import com.daon.daonpay.global.jwt.JwtProvider;
import com.daon.daonpay.global.util.ErrorResponseWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 보안 설정 클래스
 * 애플리케이션의 인증 및 인가 정책과 JWT 필터 체인을 설정
 */

@Configuration

// Spring Security 활성화
@EnableWebSecurity

// @PreAuthorize, @Secured 등 메서드 단위 권한 검사 기능 활성화

@EnableMethodSecurity
// final 필드를 대상으로 생성자를 자동 생성하여 의존성 주입
@RequiredArgsConstructor
public class SecurityConfig {

    // 사용처(place) 관리 API 경로
    // 등록(POST), 상태 변경(PATCH), 상품 추가(POST) / 삭제(DELETE) 는 관리자 전용
    // 조회(GET)는 이 규칙에 포함하지 않고 기존 정책(로그인 필요)을 그대로 유지
    private static final String[] PLACE_PATTERNS = {"/api/places", "/api/places/**"};

    // JWT 필터 생성에 필요한 의존성
    // 두 JWT 필터는 Bean 으로 등록하지 않고 filterChain 안에서 직접 생성한다.
    // (Bean 이면 서블릿 컨테이너에 자동 등록되어 Security 체인 밖에서도 실행됨)
    private final JwtProvider jwtProvider;
    private final ErrorResponseWriter errorResponseWriter;

    // 인증되지 않은 사용자가 보호된 리소스에 접근할 때 실행되는 핸들러 (401)
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    // 인증은 완료되었지만 접근 권한이 없는 사용자의 요청을 처리하는 핸들러 (403)
    private final CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * Spring Security 필터 체인 설정

     * @param http HTTP 보안 설정 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 설정 중 발생할 수 있는 예외
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    // JWT를 검증하고 인증 정보를 SecurityContext에 등록하는 필터
    JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider);

    // JWT 인증 과정에서 발생한 예외를 공통 에러 응답으로 변환하는 필터
    JwtExceptionFilter jwtExceptionFilter = new JwtExceptionFilter(errorResponseWriter);

    String admin = Role.ADMIN.name();

        http
                // REST API 서버이므로 CSRF 보호 기능 비활성화
                .csrf(csrf -> csrf.disable())

                // 기본 로그인 페이지를 사용하는 Form Login 비활성화
                .formLogin(form -> form.disable())

                // HTTP Basic 인증 방식 비활성화
                .httpBasic(basic -> basic.disable())

                // 세션을 사용하지 않는 Stateless 방식으로 설정
                // 매 요청마다 JWT를 통해 사용자 인증 수행
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // 회원가입 및 로그인 API는 인증 없이 접근 허용
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/login",
                                "/api/users/signup"
                        ).permitAll()

                        // Swagger UI 및 API 명세 조회 경로는 인증 없이 접근 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 관리자 전용 API
                        // ADMIN 역할을 가진 사용자만 접근 가능
                        .requestMatchers("/api/admin/**")
                        .hasRole(Role.ADMIN.name())

                        // 사용처 관리 API
                        // 등록, 수정, 상태 변경, 상품 추가 및 삭제는 ADMIN만 가능
                        .requestMatchers(HttpMethod.POST, PLACE_PATTERNS)
                        .hasRole(Role.ADMIN.name())

                        .requestMatchers(HttpMethod.PUT, PLACE_PATTERNS)
                        .hasRole(Role.ADMIN.name())

                        .requestMatchers(HttpMethod.PATCH, PLACE_PATTERNS)
                        .hasRole(Role.ADMIN.name())

                        .requestMatchers(HttpMethod.DELETE, PLACE_PATTERNS)
                        .hasRole(Role.ADMIN.name())

                        .requestMatchers(HttpMethod.POST, "/api/terminals/authenticate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/qr/verify").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/qr/verifications/*").permitAll()
                        .requestMatchers("/api/terminals/**").hasRole("ADMIN")

                        // 위에서 별도로 허용한 경로를 제외한 모든 API는 인증 필요
                        .anyRequest().authenticated()
                )

                // 인증 및 인가 실패 시 실행할 예외 처리 핸들러 설정
                .exceptionHandling(ex -> ex

                        // 인증되지 않은 사용자가 보호된 API에 접근하면 401 처리
                        .authenticationEntryPoint(authenticationEntryPoint)

                        // 인증은 되었지만 권한이 부족하면 403 처리
                        .accessDeniedHandler(accessDeniedHandler)
                )

                /**
                 JWT 인증 관련 필터 등록
                 실행 순서: JwtExceptionFilter
                           -> JwtAuthenticationFilter
                          -> UsernamePasswordAuthenticationFilter
                 JWT 인증 필터에서 발생한 예외를 앞선 예외 필터가 처리하도록 구성
                 */

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        jwtExceptionFilter,
                        JwtAuthenticationFilter.class
                );

        // 설정한 보안 정책을 기반으로 SecurityFilterChain 생성 및 반환
        return http.build();
    }

    /**
     * 비밀번호 암호화 및 검증에 사용할 PasswordEncoder 등록

     * @return BCrypt 방식의 비밀번호 암호화 객체
     */

    @Bean
    public PasswordEncoder passwordEncoder() {

        // BCrypt 해시 알고리즘을 사용하여 비밀번호 암호화
        // 회원가입 시 encode(), 로그인 시 matches()로 사용
        return new BCryptPasswordEncoder();
    }
}
