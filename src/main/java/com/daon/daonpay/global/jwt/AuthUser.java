package com.daon.daonpay.global.jwt;

import com.daon.daonpay.domain.user.entity.Role;

/**
 * 인증된 사용자 정보를 저장하는 객체
 * Spring Security의 SecurityContext에서 principal로 사용
 * 컨트롤러에서 @AuthenticationPrincipal AuthUser로 사용자 정보에 접근 가능
 */

public record AuthUser(

        // 인증된 사용자의 고유 식별자
        Long userId,

        // 인증된 사용자의 역할 (ADMIN, GENERAL, VULNERABLE 등)
        Role role

) {

    // 현재 인증된 사용자가 관리자인지 확인
    // 역할이 ADMIN이면 true, 아니면 false 반환
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    // 현재 인증된 사용자가 취약계층 사용자인지 확인
    // 역할이 VULNERABLE이면 true, 아니면 false 반환
    public boolean isVulnerable() {
        return role == Role.VULNERABLE;
    }
}