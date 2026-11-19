package com.daon.daonpay.domain.user.dto.response;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.entity.User;

// 회원가입 성공(201) 시 반환하는 응답 DTO
// 비밀번호 등 민감 정보는 포함하지 않는다.
public record UserResponse(

        // 가입한 사용자의 고유 식별자
        Long userId,

        // 가입한 사용자의 이메일
        String email,

        // 가입한 사용자의 이름
        String name,

        // 가입한 사용자의 역할
        Role role

) {

    // User 엔티티에서 응답에 필요한 데이터만 추출하여 DTO 로 변환
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}