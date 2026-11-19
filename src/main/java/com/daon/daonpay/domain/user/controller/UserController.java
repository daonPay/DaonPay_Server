package com.daon.daonpay.domain.user.controller;

import com.daon.daonpay.domain.user.dto.request.LoginRequest;
import com.daon.daonpay.domain.user.dto.request.SignupRequest;
import com.daon.daonpay.domain.user.dto.response.LoginResponse;
import com.daon.daonpay.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;import com.daon.daonpay.domain.user.dto.response.UserResponse;
import org.springframework.http.HttpStatus;


@RestController

// 모든 API 요청의 공통 URL 경로 설정
@RequestMapping("/api/users")

// final 필드를 대상으로 생성자를 자동 생성하여 의존성 주입
@RequiredArgsConstructor
public class UserController {

    // 사용자 회원가입 및 로그인 비즈니스 로직을 담당하는 서비스
    private final UserService userService;

    // 회원가입 API
    // POST /api/users/signup
    // 성공 시 201(Created) 과 함께 UserResponse 본문을 반환
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(

            // 요청 본문(JSON)을 SignupRequest 객체로 변환하고 유효성 검사 수행
            @Valid @RequestBody SignupRequest request) {

        // 회원가입 비즈니스 로직 실행 후 가입한 사용자 정보를 응답 본문에 담아 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(request));
    }

    // 로그인 API
    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(

            // 요청 본문(JSON)을 LoginRequest 객체로 변환하고 유효성 검사 수행
            @Valid @RequestBody LoginRequest request) {

        // 로그인 비즈니스 로직 실행 후 생성된 토큰과 사용자 정보를 반환
        // 로그인 성공 시 HTTP 상태 코드 200(OK) 반환
        return ResponseEntity.ok(userService.login(request));
    }
}