package com.daon.daonpay.global.config;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 개발 및 테스트 환경에서 사용할 계정을 자동 생성하는 클래스

 * - local, dev 프로필에서만 실행
 * - app.seed.accounts.enabled=true 설정이 있어야 실행
 * - 계정 비밀번호는 환경 변수로 전달
 * - 관리자 계정은 별도 설정이 활성화된 local 환경에서만 생성
 */

@Slf4j
@Component
@Profile({"local", "dev"})
@ConditionalOnProperty(prefix = "app.seed.accounts", name = "enabled", havingValue = "true")
public class TestAccountSeeder implements ApplicationRunner {

    // 시드 계정 생성 시 사용할 최소 비밀번호 길이
    private static final int MIN_PASSWORD_LENGTH = 12;

    // 역할별 생성할 테스트 계정 수
    private static final int ACCOUNTS_PER_ROLE = 3;

    // 관리자 계정 생성을 허용하는 프로필
    private static final String LOCAL_PROFILE = "local";

    // 기본적으로 생성할 일반 사용자 및 취약 사용자 계정 역할
    // 새로운 Role이 추가되어도 목록에 직접 추가하지 않으면 계정이 생성되지 않음
    private static final List<Role> DEFAULT_ROLES = List.of(Role.GENERAL, Role.VULNERABLE);

    // 별도 설정이 활성화되었을 때만 생성할 관리자 역할
    private static final List<Role> PRIVILEGED_ROLES = List.of(Role.ADMIN);

    // 테스트 계정을 저장하고 조회하는 Repository
    private final UserRepository userRepository;

    // 비밀번호를 암호화하기 위한 PasswordEncoder
    private final PasswordEncoder passwordEncoder;

    // 테스트 계정 생성에 공통으로 사용할 비밀번호
    private final String password;

    // 관리자 계정 생성 여부
    private final boolean includePrivileged;

    /**
     * 필요한 의존성과 설정값을 주입받는 생성자다.

     * @param userRepository 사용자 데이터 조회 및 저장을 위한 Repository
     * @param passwordEncoder 비밀번호 암호화 객체
     * @param environment 현재 실행 환경의 프로필 정보
     * @param password 테스트 계정에 사용할 공통 비밀번호
     * @param adminRequested 관리자 계정 생성 요청 여부
     */

    public TestAccountSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment,
            @Value("${app.seed.accounts.password:}") String password,
            @Value("${app.seed.accounts.admin:false}") boolean adminRequested) {

        // 비밀번호가 설정되지 않았거나 최소 길이를 충족하지 못하면 실행을 중단
        // 안전하지 않은 기본 비밀번호로 계정을 생성하는 것을 방지
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "테스트 계정 시드를 쓰려면 APP_SEED_ACCOUNTS_PASSWORD(app.seed.accounts.password)를 "
                            + MIN_PASSWORD_LENGTH + "자 이상으로 설정해야 합니다.");
        }

        // 현재 실행 환경이 local 프로필인지 확인한다.
        boolean isLocal = environment.acceptsProfiles(Profiles.of(LOCAL_PROFILE));

        // local이 아닌 환경에서 관리자 계정 생성을 요청하면 경고를 출력한다.
        if (adminRequested && !isLocal) {
            log.warn("관리자 시드는 local 프로필에서만 생성됩니다. 요청을 무시합니다.");
        }

        // 생성자에서 전달받은 의존성을 필드에 저장한다.
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.password = password;

        // 관리자 계정은 생성 요청이 있었고 local 프로필일 때만 생성하도록 설정한다.
        this.includePrivileged = adminRequested && isLocal;
    }

    /**
     * 애플리케이션 실행 이후 테스트 계정을 생성

     * ApplicationRunner를 구현하므로 Spring Boot 애플리케이션이 실행된 후
     * 전달받은 계정 생성 작업을 수행한다.

     * 1. 기본 테스트 계정 생성
     * 2. GENERAL, VULNERABLE 역할별 계정 생성
     * 3. 조건에 해당하면 ADMIN 계정 생성
     */

    @Override
    public void run(ApplicationArguments args) {

        // 별도의 회원가입 과정 없이 로그인 테스트에 사용할 계정
        // 로그인 시 이메일 입력란에 "test"를 입력
        createIfAbsent("test", "테스트", Role.GENERAL);

        // 기본 역할에 해당하는 테스트 계정들을 생성
        seed(DEFAULT_ROLES);

        // 관리자 계정 생성 조건을 충족한 경우에만 관리자 계정을 생성
        if (includePrivileged) {
            seed(PRIVILEGED_ROLES);
        }

        // 테스트 계정 생성 작업이 끝났음을 로그로 남김
        log.info("테스트 계정 시드 처리 완료 (관리자 포함: {})", includePrivileged);
    }

    /**
     * 전달받은 역할 목록을 기준으로 테스트 계정을 생성한다.

     * 역할 이름을 이메일 접두사로 사용하며,
     * 각 역할마다 ACCOUNTS_PER_ROLE에 지정된 수만큼 생성한다.

     * 예시:
     * GENERAL -> general1@test.com ~ general3@test.com
     * VULNERABLE -> vulnerable1@test.com ~ vulnerable3@test.com
     * ADMIN -> admin1@test.com ~ admin3@test.com

     * @param roles 테스트 계정을 생성할 역할 목록
     */

    private void seed(List<Role> roles) {
        for (Role role : roles) {

            // 역할 이름을 소문자로 변환하여 계정 이메일 접두사로 사용
            String prefix = role.name().toLowerCase();

            // 역할마다 지정된 개수만큼 계정을 생성
            for (int i = 1; i <= ACCOUNTS_PER_ROLE; i++) {

                // 이메일과 이름을 규칙에 맞게 생성하여 계정 생성 메서드를 호출
                createIfAbsent(prefix + i + "@test.com", prefix + " 테스트" + i, role);
            }
        }
    }

    /**
     * 전달받은 이메일의 계정이 존재하지 않을 때만 생성

     * 이미 존재하는 계정은 중복 생성하지 않음
     * 계정 저장 중 이메일 중복 예외가 발생한 경우에도
     * 실제 계정이 존재하는지 확인하여 정상적인 중복 상황으로 처리

     * @param email 계정 이메일
     * @param name 계정 이름
     * @param role 계정 역할
     */

    private void createIfAbsent(String email, String name, Role role) {

        // 해당 이메일을 가진 계정이 이미 존재하면 생성 작업을 건너뜀
        if (userRepository.existsByEmail(email)) {
            return;
        }

        try {

            // 사용자 객체를 생성하고 DB에 저장
            // 비밀번호는 저장 전에 PasswordEncoder를 통해 암호화
            userRepository.save(User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .name(name)
                    .role(role)
                    .build());

            // 계정 생성 성공 로그를 출력
            log.info("테스트 계정 생성: {} ({})", email, role);

        } catch (DataIntegrityViolationException e) {

            // 여러 애플리케이션 인스턴스가 동시에 실행되는 경우
            // 두 인스턴스가 같은 계정을 생성하려고 시도할 수 있음
            // 이때 DB의 이메일 UNIQUE 제약 조건에 의해 중복 저장이 방지

            // 예외 발생 후 실제 계정이 존재하는지 다시 확인
            // 계정이 존재한다면 다른 인스턴스가 먼저 생성한 것으로 간주
            if (!userRepository.existsByEmail(email)) {

                // 계정이 존재하지 않는다면 단순 중복 문제가 아니므로 예외를 다시 발생 시킴
                throw e;
            }

            // 다른 인스턴스에서 이미 계정을 생성한 경우 정상 처리
            log.info("이미 생성된 테스트 계정: {}", email);
        }
    }
}