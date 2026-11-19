package com.daon.daonpay.global.config;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
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
 * 개발/테스트용 계정 시드
 * - local, dev 프로필 + app.seed.accounts.enabled=true 일 때만 등록된다.
 * - 비밀번호는 환경 변수(APP_SEED_ACCOUNTS_PASSWORD)로 주입하며, 최초 생성 시에만 쓰인다.
 * - 관리자 계정은 app.seed.accounts.admin=true 이면서 local 단독 프로필일 때만 생성한다.
 *   (dev가 함께 켜진 경우에는 local과 섞여 있어도 생성하지 않는다)
 */
@Slf4j
@Component
@Profile({"local", "dev"})
@ConditionalOnProperty(prefix = "app.seed.accounts", name = "enabled", havingValue = "true")
public class TestAccountSeeder implements ApplicationRunner {

    static final int MIN_PASSWORD_LENGTH = 12;

    // BCrypt는 UTF-8 기준 72바이트까지만 사용한다 (한글은 글자당 3바이트)
    static final int MAX_PASSWORD_BYTES = 72;

    private static final int ACCOUNTS_PER_ROLE = 3;
    private static final String LOCAL_PROFILE = "local";
    private static final String DEV_PROFILE = "dev";

    // User 엔티티의 이메일 유니크 제약 이름과 일치해야 한다
    private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_users_email";

    // 시드 대상 역할은 명시적으로 나열한다 (Role이 추가돼도 여기에 넣기 전에는 생성되지 않음)
    private static final List<Role> DEFAULT_ROLES = List.of(Role.GENERAL, Role.VULNERABLE);
    private static final List<Role> PRIVILEGED_ROLES = List.of(Role.ADMIN);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String password;
    private final boolean includePrivileged;

    public TestAccountSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment,
            @Value("${app.seed.accounts.password:}") String password,
            @Value("${app.seed.accounts.admin:false}") boolean adminRequested) {

        validatePassword(password);

        // local 단독일 때만 관리자 허용. dev가 하나라도 켜져 있으면 차단한다.
        boolean allowPrivileged = environment.acceptsProfiles(Profiles.of(LOCAL_PROFILE))
                && !environment.acceptsProfiles(Profiles.of(DEV_PROFILE));

        if (adminRequested && !allowPrivileged) {
            log.warn("관리자 시드는 local 단독 프로필에서만 생성됩니다. 요청을 무시합니다.");
        }

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.password = password;
        this.includePrivileged = adminRequested && allowPrivileged;
    }

    static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "APP_SEED_ACCOUNTS_PASSWORD(app.seed.accounts.password)가 비어 있습니다.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "테스트 계정 비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalStateException(
                    "테스트 계정 비밀번호는 UTF-8 기준 " + MAX_PASSWORD_BYTES
                            + "바이트 이하여야 합니다. (한글은 글자당 3바이트)");
        }
    }

    // 트랜잭션을 걸지 않는다: 계정마다 save()가 개별 커밋되므로
    // 한 건이 중복으로 실패해도 나머지 계정 생성이 롤백되지 않는다.
    @Override
    public void run(ApplicationArguments args) {
        createIfAbsent("test", "테스트", Role.GENERAL);

        seed(DEFAULT_ROLES);
        if (includePrivileged) {
            seed(PRIVILEGED_ROLES);
        }

        log.info("테스트 계정 시드 처리 완료 (관리자 포함: {})", includePrivileged);
    }

    private void seed(List<Role> roles) {
        for (Role role : roles) {
            // 로케일에 따라 접두사가 달라지지 않도록 Locale.ROOT 고정
            String prefix = role.name().toLowerCase(Locale.ROOT);
            for (int i = 1; i <= ACCOUNTS_PER_ROLE; i++) {
                createIfAbsent(prefix + i + "@test.com", prefix + " 테스트" + i, role);
            }
        }
    }

    private void createIfAbsent(String email, String name, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        try {
            userRepository.save(User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .name(name)
                    .role(role)
                    .build());
            log.info("테스트 계정 생성: {} ({})", email, role);
        } catch (DataIntegrityViolationException e) {
            // 이메일 유니크 제약 위반이고 실제로 행이 존재할 때만 정상 중복으로 처리한다.
            // 그 외 DB 오류(NOT NULL 위반, 길이 초과 등)는 그대로 전파한다.
            if (!isEmailUniqueViolation(e) || !userRepository.existsByEmail(email)) {
                throw e;
            }
            log.info("이미 생성된 테스트 계정: {}", email);
        }
    }

    private static boolean isEmailUniqueViolation(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve) {
                String constraint = cve.getConstraintName();
                if (constraint != null && containsIgnoreCase(constraint, EMAIL_UNIQUE_CONSTRAINT)) {
                    return true;
                }
            }
            if (t.getMessage() != null && containsIgnoreCase(t.getMessage(), EMAIL_UNIQUE_CONSTRAINT)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsIgnoreCase(String source, String target) {
        return source.toLowerCase(Locale.ROOT).contains(target);
    }
}