package com.daon.daonpay.global.jwt;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 생성 및 검증을 담당하는 클래스
 * 액세스 토큰 발급, 토큰 검증, 인증된 사용자 정보 추출 기능 제공
 */

@Component
public class JwtProvider {

    // JWT Payload에 사용자의 역할을 저장할 때 사용하는 Claim 이름
    private static final String ROLE_CLAIM = "role";

    // JWT 서명 및 검증에 사용하는 비밀키
    private final SecretKey key;

    // 액세스 토큰의 유효 기간 (밀리초)
    private final long accessExpirationMs;

    /**
     * JWT 설정값을 주입받아 초기화

     * @param secret JWT 서명에 사용할 비밀키 문자열 (기존 설정과 동일하게 UTF-8 문자열로 사용, Base64 해석 안 함)
     * @param accessExpirationMs 액세스 토큰 만료 시간 (밀리초), 기존 설정 키 jwt.access-expiration-ms 유지
     */

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms:3600000}") long accessExpirationMs) {

        // 기존 설정값을 그대로 쓸 수 있도록 UTF-8 바이트로 HMAC 서명 키 생성
        // (Base64 로 디코딩하면 기존 설정값으로 초기화에 실패하거나 서명 키가 달라져 기존 토큰이 무효화됨)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        this.accessExpirationMs = accessExpirationMs;
    }

    /**
     * JWT 액세스 토큰 생성

     * @param userId 토큰을 발급받는 사용자의 ID
     * @param role 사용자의 역할 정보
     * @return 생성된 JWT 액세스 토큰
     */

    public String createAccessToken(Long userId, Role role) {

        // 토큰 발급 시점의 현재 시간
        Date now = new Date();

        return Jwts.builder()

                // 토큰의 주체(Subject)에 사용자 ID 저장
                .subject(String.valueOf(userId))

                // 토큰의 Payload에 사용자 역할 정보 저장
                .claim(ROLE_CLAIM, role.name())

                // 토큰 발급 시간 설정
                .issuedAt(now)

                // 현재 시간에 유효 기간을 더하여 토큰 만료 시간 설정
                .expiration(new Date(now.getTime() + accessExpirationMs))

                // 비밀키를 사용하여 JWT 서명
                .signWith(key)

                // JWT 문자열 생성
                .compact();
    }

    /**
     * JWT 검증 후 인증된 사용자 정보 추출
     * 사용자 ID, 역할이 누락되었거나 변환할 수 없는 값이면 모두 INVALID_TOKEN 으로 처리

     * @param token 검증할 JWT 액세스 토큰
     * @return 사용자 ID와 역할 정보가 담긴 AuthUser 객체
     */

    public AuthUser getAuthUser(String token) {

        // 토큰 검증 후 Payload에 저장된 Claims 추출
        Claims claims = parseClaims(token);

        return new AuthUser(extractUserId(claims), extractRole(claims));
    }

    // Subject 에 저장된 사용자 ID 를 Long 으로 변환
    // Subject 가 없거나 숫자가 아니면 INVALID_TOKEN
    private Long extractUserId(Claims claims) {

        String subject = claims.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    // role Claim 을 Role Enum 으로 변환
    // Role.valueOf(null) 은 NullPointerException 을 던지므로 존재 여부와 타입을 먼저 확인한 뒤 변환
    private Role extractRole(Claims claims) {

        Object raw = claims.get(ROLE_CLAIM);

        // role 클레임이 없거나, 문자열이 아니거나, 비어 있는 경우
        if (!(raw instanceof String roleName) || roleName.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        try {
            return Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {

            // Role 에 정의되지 않은 값인 경우
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT 서명 및 유효 기간 검증 후 Claims 추출

     * @param token 검증할 JWT 토큰
     * @return 토큰의 Payload에 저장된 Claims 정보

     * @throws CustomException 토큰이 만료되었거나 유효하지 않은 경우
     */

    private Claims parseClaims(String token) {

        try {

            return Jwts.parser()

                    // JWT 서명 검증에 사용할 비밀키 설정
                    .verifyWith(key)

                    // JWT 파서 생성
                    .build()

                    // 전달받은 JWT의 서명과 만료 여부 검증
                    .parseSignedClaims(token)

                    // 검증된 토큰의 Payload에서 Claims 추출
                    .getPayload();

        } catch (ExpiredJwtException e) {

            // 토큰의 유효 기간이 만료된 경우
            // 기존 에러 코드(TOKEN_EXPIRED / AUTH_003)를 그대로 사용해 재로그인 처리 중인 클라이언트 동작 유지
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);

        } catch (JwtException | IllegalArgumentException e) {

            // 서명이 잘못되었거나 토큰 형식이 올바르지 않은 경우
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}