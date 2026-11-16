package global.config;

import global.exception.CustomAccessDeniedHandler;
import global.exception.CustomAuthenticationEntryPoint;
import global.exception.JwtExceptionFilter;
import global.jwt.JwtAuthenticationFilter;
import global.jwt.JwtProvider;
import global.util.ErrorResponseWriter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


 // Spring Security 보안 설정 클래스
 // 애플리케이션의 인증 및 인가 정책을 설정

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 인증되지 않은 사용자가 보호된 리소스에 접근할 때 실행되는 핸들러
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    // 인증은 완료되었지만 접근 권한이 없는 사용자의 요청을 처리하는 핸들러
    private final CustomAccessDeniedHandler accessDeniedHandler;

    private final JwtProvider jwtProvider;
    private final ErrorResponseWriter errorResponseWriter;


    /**
     * Spring Security 필터 체인 설정
     *
     * @param http HTTP 보안 설정 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 설정 중 발생할 수 있는 예외
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider);
        JwtExceptionFilter jwtExceptionFilter = new JwtExceptionFilter(errorResponseWriter);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()   // 본인 경로로 수정
                        .anyRequest().authenticated()
                )

                // 인증/인가 실패 시 JSON 에러 응답
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 필터 순서: JwtExceptionFilter → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}