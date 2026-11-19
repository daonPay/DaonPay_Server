package com.daon.daonpay.domain.user.dto.request;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.validation.MaxUtf8Bytes;
import com.daon.daonpay.domain.user.validation.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * role 은 선택값이며, 비어 있으면 GENERAL 로 처리한다.
 * ADMIN 은 가입 시 지정할 수 없다 (UserService 에서 차단).

 * password 는 글자 수(8~64자)와 함께 UTF-8 기준 72바이트 이하여야 한다. (BCrypt 입력 한도)
 * 한글은 글자당 3바이트이므로 한글만으로는 최대 24자까지 가능하다.
 */

public record SignupRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 64) @MaxUtf8Bytes(PasswordPolicy.MAX_BYTES) String password,
        @NotBlank @Size(max = 50) String name,
        Role role) {
}