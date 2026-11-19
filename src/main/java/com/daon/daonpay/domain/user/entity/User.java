package com.daon.daonpay.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.UniqueConstraint;

// 사용자 정보를 저장하는 JPA 엔티티
@Entity
@Table(
        name = "users",

        // 이메일 중복을 막는 유니크 제약. 이름을 고정해 두어 동시 가입으로 인한 중복만 DUPLICATE_EMAIL 로 변환한다.
        uniqueConstraints = @UniqueConstraint(
                name = User.EMAIL_UNIQUE_CONSTRAINT, columnNames = "email")
)
@Getter

// 기본 생성자 자동 생성 (JPA에서 엔티티 조회 및 생성 시 사용)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    // 이메일 유니크 제약 이름 (UserService 에서 제약 위반 원인을 구분할 때 사용)
    public static final String EMAIL_UNIQUE_CONSTRAINT = "uk_users_email";

    // 사용자 고유 식별자 (Primary Key)
    @Id

    // 데이터베이스에서 기본 키 값을 자동 생성 (AUTO_INCREMENT)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 이메일
    // 필수 입력값이며 중복을 허용하지 않고 최대 100자까지 저장
    @Column(nullable = false, length = 100)
    private String email;

    // 사용자 비밀번호
    // 필수 입력값
    @Column(nullable = false)
    private String password;

    // 사용자 이름
    // 필수 입력값이며 최대 50자까지 저장
    @Column(nullable = false, length = 50)
    private String name;

    // 사용자 권한 또는 역할
    // Enum 값을 문자열 형태로 데이터베이스에 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // Builder 패턴을 이용한 객체 생성자
    // 필요한 사용자 정보를 전달하여 User 객체 생성
    @Builder
    private User(String email, String password, String name, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }
}