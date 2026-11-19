package com.daon.daonpay.domain.user.validation;

import java.nio.charset.StandardCharsets;

/**
 * 비밀번호 정책
 * BCrypt 의 입력 한도는 UTF-8 기준 72바이트이다. (한글은 글자당 3바이트라 24자까지)
 * 가입(입력 오류)과 로그인(로그인 실패)에서 같은 기준을 사용하도록 한 곳에서 관리한다.
 */

public final class PasswordPolicy {

    // BCrypt 가 처리할 수 있는 최대 바이트 수
    public static final int MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    // 비밀번호가 UTF-8 기준 최대 바이트 수를 넘는지 확인
    public static boolean exceedsMaxBytes(String rawPassword) {
        return rawPassword != null
                && rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }
}