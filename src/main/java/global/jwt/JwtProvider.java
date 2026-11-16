package global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 생성 및 검증을 담당하는 클래스
 * 액세스 토큰 생성, 토큰 검증, 인증 객체 변환 기능 제공
 */

@Component
public class JwtProvider {

    // JWT 서명 및 검증에 사용하는 비밀키
    private final SecretKey key;

    // 액세스 토큰의 유효 기간 (밀리초)
    private final long accessExpirationMs;

    /**
     * JWT 설정값을 주입받아 초기화

     * @param secret JWT 서명에 사용할 비밀키
     * @param accessExpirationMs 액세스 토큰 만료 시간
     */

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms}") long accessExpirationMs
    ) {
        // 문자열 형태의 비밀키를 HMAC SHA 알고리즘에 사용할 SecretKey로 변환
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
    }

    /**
     * 액세스 토큰 생성

     * @param userId 토큰을 발급받는 사용자의 ID
     * @param role 사용자의 권한 정보
     * @return 생성된 JWT 액세스 토큰
     */

    public String createAccessToken(Long userId, String role) {

        // 토큰 발급 시점의 현재 시간
        Date now = new Date();

        return Jwts.builder()
                // 토큰의 주체(Subject)에 사용자 ID 저장
                .subject(String.valueOf(userId))

                // 토큰에 사용자 권한 정보 저장
                .claim("role", role)

                // 토큰 발급 시간 설정
                .issuedAt(now)

                // 현재 시간에 유효 기간을 더해 만료 시간 설정
                .expiration(new Date(now.getTime() + accessExpirationMs))

                // 비밀키를 사용하여 토큰에 서명
                .signWith(key)

                // JWT 문자열 생성
                .compact();
    }

    /**
     * JWT 검증 및 Claims 추출

     * @param token 검증할 JWT 토큰
     * @return 토큰에 저장된 Claims 정보

     * 만료된 토큰은 ExpiredJwtException,
     * 위조되거나 형식이 잘못된 토큰은 JwtException 발생
     */

    public Claims parseClaims(String token) {

        return Jwts.parser()
                // 서명 검증에 사용할 비밀키 설정
                .verifyWith(key)

                // JWT 파서 생성
                .build()

                // 전달받은 토큰의 서명과 유효 기간 검증 후 Claims 추출
                .parseSignedClaims(token)

                // 토큰의 Payload에 저장된 Claims 반환
                .getPayload();
    }

    /**
     * JWT를 기반으로 Spring Security 인증 객체 생성

     * @param token 인증에 사용할 JWT 토큰
     * @return Spring Security에서 사용하는 Authentication 객체
     */

    public Authentication getAuthentication(String token) {

        // 토큰 검증 후 사용자 정보(Claims) 추출
        Claims claims = parseClaims(token);

        // 토큰에 저장된 사용자 권한 정보 추출
        String role = claims.get("role", String.class);

        return new UsernamePasswordAuthenticationToken(
                claims.getSubject(),   // principal = 사용자 ID
                null,                  // credentials = 별도의 비밀번호 정보 없음

                // 사용자 권한을 Spring Security에서 인식하는 GrantedAuthority 형태로 변환
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}