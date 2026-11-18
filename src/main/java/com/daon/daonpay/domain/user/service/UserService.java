package com.daon.daonpay.domain.user.service;

import com.daon.daonpay.domain.user.dto.request.LoginRequest;
import com.daon.daonpay.domain.user.dto.request.SignupRequest;
import com.daon.daonpay.domain.user.dto.response.LoginResponse;
import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import com.daon.daonpay.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 사용자 회원가입 및 로그인 관련 비즈니스 로직을 처리하는 서비스
@Service

// final 필드를 대상으로 생성자를 자동 생성하여 의존성 주입
@RequiredArgsConstructor
public class UserService {

    // 사용자 정보를 조회하고 저장하는 Repository
    private final UserRepository userRepository;

    // 비밀번호 암호화 및 입력 비밀번호 검증을 담당하는 객체
    private final PasswordEncoder passwordEncoder;

    // JWT 액세스 토큰 생성을 담당하는 객체
    private final JwtProvider jwtProvider;

    // 회원가입 처리
    // 데이터베이스 변경 작업이 포함되므로 트랜잭션 적용
    @Transactional
    public Long signup(SignupRequest request) {

        // 요청에 역할이 지정되지 않았다면 기본 역할을 GENERAL로 설정
        Role role = request.role() == null ? Role.GENERAL : request.role();

        // 자체 회원가입이 허용되지 않는 역할인지 확인
        // 허용되지 않는 역할이면 INVALID_ROLE 예외 발생
        if (!role.isSelfSignupAllowed()) {
            throw new CustomException(ErrorCode.INVALID_ROLE);
        }

        // 입력한 이메일이 이미 가입된 계정에 사용되고 있는지 확인
        // 중복 이메일이면 DUPLICATE_EMAIL 예외 발생
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 회원가입에 필요한 사용자 객체 생성
        User user = User.builder()
                .email(request.email())

                // 비밀번호를 암호화한 후 저장
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(role)
                .build();

        // 생성한 사용자 정보를 데이터베이스에 저장하고 사용자 ID 반환
        return userRepository.save(user).getId();
    }

    // 로그인 처리
    // 데이터 조회만 수행하므로 읽기 전용 트랜잭션 적용
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        // 입력한 이메일을 기준으로 사용자 조회
        // 해당 이메일의 사용자가 없으면 LOGIN_FAILED 예외 발생
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        // 입력한 비밀번호와 데이터베이스에 저장된 암호화 비밀번호 비교
        // 일치하지 않으면 LOGIN_FAILED 예외 발생
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        // 로그인한 사용자의 ID와 역할을 기반으로 JWT 액세스 토큰 생성
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());

        // 생성한 액세스 토큰과 사용자 정보를 로그인 응답 DTO로 변환하여 반환
        return LoginResponse.of(accessToken, user);
    }
}