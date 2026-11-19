package com.daon.daonpay.domain.user.entity;

/**
 * 사용자 역할
 * - ADMIN      : 관리자 (관리 페이지, 사용처 관리)
 * - GENERAL    : 일반인
 * - VULNERABLE : 취약계층 (지원금 대상, 음성 안내 등 접근성 기능)
 */

public enum Role {
    ADMIN,
    GENERAL,
    VULNERABLE;

    private static final String PREFIX = "ROLE_";

    // Spring Security Authority 문자열 (hasRole("ADMIN") 과 매칭됨)
    public String authority() {
        return PREFIX + name();
    }

    // 회원가입 시 직접 선택할 수 있는 역할인지 (ADMIN 은 가입 불가)
    public boolean isSelfSignupAllowed() {
        return this != ADMIN;
    }
}
