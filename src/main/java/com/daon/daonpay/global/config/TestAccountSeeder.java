package com.daon.daonpay.global.config;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개발 및 테스트 환경에서 사용할 테스트 계정을 자동 생성하는 클래스

 * - local, dev 프로필에서만 실행된다.
 * - 운영 환경에서는 빈 자체가 생성되지 않는다.
 * - 이미 존재하는 이메일은 건너뛰므로 중복 계정 생성을 방지한다.
 * - 애플리케이션 실행 시 자동으로 테스트 계정을 생성한다.
 */

// Lombok을 통해 로그를 기록할 수 있는 log 객체 자동 생성
@Slf4j

// Spring 컨테이너에 빈으로 등록
@Component

// local 또는 dev 프로필에서만 활성화
@Profile({"local", "dev"})

// final 필드를 사용하는 생성자 자동 생성
@RequiredArgsConstructor
public class TestAccountSeeder implements ApplicationRunner {

    // 개발용 공통 비밀번호 (회원가입 시 비밀번호 최소 길이 8자 충족)
    private static final String DEFAULT_PASSWORD = "test1234!";

    // 역할별 생성할 테스트 계정 수
    private static final int ACCOUNTS_PER_ROLE = 3;

    // 사용자 정보를 데이터베이스에 저장하고 조회하기 위한 Repository
    private final UserRepository userRepository;

    // 비밀번호를 암호화하기 위한 PasswordEncoder
    private final PasswordEncoder passwordEncoder;

    /**
     * 애플리케이션 실행이 완료된 후 자동으로 호출되는 메서드
     * 기본 테스트 계정과 역할별 테스트 계정을 생성

     * @param args 애플리케이션 실행 시 전달된 인자
     */

    @Override

    // 계정 생성 작업 전체를 하나의 트랜잭션으로 처리
    @Transactional
    public void run(ApplicationArguments args) {

        // 별도의 회원가입 없이 로그인 테스트에 사용할 기본 계정 생성
        // 로그인 시 이메일 입력란에 "test" 입력
        createIfAbsent("test", "테스트", Role.GENERAL);

        // Role에 정의된 모든 역할을 순회하며 역할별 테스트 계정 생성
        // 예: admin1@test.com, general1@test.com, vulnerable1@test.com
        for (Role role : Role.values()) {

            // 역할 이름을 소문자로 변환하여 이메일 및 계정 이름의 접두사로 사용
            String prefix = role.name().toLowerCase();

            // 역할별로 설정된 계정 수만큼 반복하여 계정 생성
            for (int i = 1; i <= ACCOUNTS_PER_ROLE; i++) {

                createIfAbsent(
                        prefix + i + "@test.com", // 테스트 계정 이메일
                        prefix + " 테스트" + i,    // 테스트 계정 이름
                        role                      // 계정에 부여할 역할
                );
            }
        }

        // 테스트 계정 생성 작업이 완료되었음을 로그로 출력
        log.info("테스트 계정 시드 데이터 처리 완료");
    }

    /**
     * 테스트 계정이 존재하지 않는 경우에만 새 계정을 생성하는 메서드

     * 동일한 이메일의 계정이 이미 존재하면 생성하지 않음

     * @param email 계정 이메일
     * @param name  사용자 이름
     * @param role  사용자 역할
     */

    private void createIfAbsent(String email, String name, Role role) {

        // 데이터베이스에 동일한 이메일을 사용하는 계정이 존재하는지 확인
        if (userRepository.existsByEmail(email)) {

            // 이미 존재하는 계정이면 추가 작업 없이 메서드 종료
            return;
        }

        // 새로운 사용자 엔티티 생성 및 데이터베이스 저장
        userRepository.save(User.builder()

                // 사용자 이메일 설정
                .email(email)

                // 비밀번호를 암호화한 후 저장
                // 평문 비밀번호가 데이터베이스에 저장되는 것을 방지
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))

                // 사용자 이름 설정
                .name(name)

                // 사용자 역할 설정
                .role(role)

                // User 객체 생성
                .build());

        // 생성된 테스트 계정의 이메일과 역할을 로그로 출력
        log.info("테스트 계정 생성: {} ({})", email, role);
    }
}