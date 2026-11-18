package com.daon.daonpay.global.jwt;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
    private final long accessTokenValidityMs;

    /**
     * JWT 설정값을 주입받아 초기화

     * @param secret JWT 서명에 사용할 Base64 인코딩 비밀키
     * @param accessTokenValidityMs 액세스 토큰 만료 시간 (밀리초)
     */

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms:3600000}") long accessTokenValidityMs) {

        // Base64로 인코딩된 비밀키를 디코딩한 후 HMAC 서명에 사용할 SecretKey 생성
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        this.accessTokenValidityMs = accessTokenValidityMs;
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
                .expiration(new Date(now.getTime() + accessTokenValidityMs))

                // 비밀키를 사용하여 JWT 서명
                .signWith(key)

                // JWT 문자열 생성
                .compact();
    }

    /**
     * JWT 검증 후 인증된 사용자 정보 추출

     * @param token 검증할 JWT 액세스 토큰
     * @return 사용자 ID와 역할 정보가 담긴 AuthUser 객체
     */

    public AuthUser getAuthUser(String token) {

        // 토큰 검증 후 Payload에 저장된 Claims 추출
        Claims claims = parseClaims(token);

        try {

            // Subject에 저장된 사용자 ID를 Long 타입으로 변환
            Long userId = Long.valueOf(claims.getSubject());

            // Claim에 저장된 역할 정보를 Role Enum으로 변환
            Role role = Role.valueOf(claims.get(ROLE_CLAIM, String.class));

            // 추출한 사용자 ID와 역할 정보로 인증 사용자 객체 생성
            return new AuthUser(userId, role);

        } catch (IllegalArgumentException e) {

            // 사용자 ID가 숫자가 아니거나 역할 정보가 유효하지 않은 경우
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT 서명 및 유효 기간 검증 후 Claims 추출

     * @param token 검증할 JWT 토큰
     * @return 토큰의 Payload에 저장된 Claims 정보
     *
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
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);

        } catch (JwtException | IllegalArgumentException e) {

            // 서명이 잘못되었거나 토큰 형식이 올바르지 않은 경우
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}