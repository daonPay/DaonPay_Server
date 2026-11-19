package com.daon.daonpay.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

// 로그인 요청 데이터를 전달하는 DTO
// 클라이언트가 전송한 이메일과 비밀번호를 전달받음
public record LoginRequest(

        // 이메일이 비어 있거나 공백만 입력되는 것을 방지
        @NotBlank
        String email,

        // 비밀번호가 비어 있거나 공백만 입력되는 것을 방지
        @NotBlank
        String password

) {
}