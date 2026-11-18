package com.daon.daonpay.domain.user.dto.response;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.entity.User;

// 로그인 성공 후 클라이언트에 반환할 응답 데이터를 정의하는 DTO
// 사용자 정보와 인증 토큰을 함께 전달
public record LoginResponse(

        // 로그인 이후 API 요청 시 인증에 사용하는 액세스 토큰
        String accessToken,

        // 로그인한 사용자의 고유 식별자
        Long userId,

        // 로그인한 사용자의 이름
        String name,

        // 로그인한 사용자의 역할
        // 프론트엔드에서 역할에 따라 화면 및 음성 안내를 구분하는 데 사용
        Role role

) {

    // User 엔티티와 액세스 토큰을 전달받아 LoginResponse 객체 생성
    // 엔티티에서 필요한 데이터만 추출하여 응답 DTO로 변환
    public static LoginResponse of(String accessToken, User user) {
        return new LoginResponse(
                accessToken,
                user.getId(),
                user.getName(),
                user.getRole()
        );
    }
}