package com.daon.daonpay.domain.user.dto.request;

import com.daon.daonpay.domain.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * role 은 선택값이며, 비어 있으면 GENERAL 로 처리한다.
 * ADMIN 은 가입 시 지정할 수 없다 (UserService 에서 차단).
 */
public record SignupRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(max = 50) String name,
        Role role) {
}