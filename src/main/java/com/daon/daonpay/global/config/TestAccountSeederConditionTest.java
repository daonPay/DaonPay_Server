package com.daon.daonpay.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;

class TestAccountSeederTest {

    // 테스트에서 기본적으로 사용할 비밀번호
    private static final String PASSWORD = "test-password-1234";

    // 실제 DB에 접근하지 않도록 가짜 UserRepository 생성
    private final UserRepository repository = mock(UserRepository.class);

    // 실제 암호화 대신 Mockito로 동작을 지정할 PasswordEncoder 생성
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    /**
     * 테스트에 필요한 환경과 설정값을 전달하여 TestAccountSeeder를 생성한다.

     * @param password       테스트에 사용할 비밀번호
     * @param adminRequested 관리자 계정 생성 요청 여부
     * @param profiles       활성화할 Spring 프로필
     * @return 테스트용 TestAccountSeeder 객체
     */

    private TestAccountSeeder seeder(String password, boolean adminRequested, String... profiles) {

        // 비밀번호 암호화 결과를 항상 "encoded"로 반환하도록 설정
        when(encoder.encode(any())).thenReturn("encoded");

        // 테스트에서 사용할 Spring 프로필 환경 구성
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(profiles);

        // 설정한 값들을 전달하여 시더 객체 생성
        return new TestAccountSeeder(repository, encoder, env, password, adminRequested);
    }

    /**
     * Repository에 save()를 통해 전달된 User 객체들을 수집한다.

     * ArgumentCaptor를 사용하여 실제 DB 저장 없이
     * 어떤 계정 정보가 저장 요청되었는지 확인할 수 있다.

     * @return save()에 전달된 User 객체 목록
     */

    private List<User> savedUsers() {

        // save() 메서드에 전달된 User 객체를 가로채기 위한 캡처 객체
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // save()가 한 번 이상 호출되었는지 확인하고 전달된 객체 수집
        verify(repository, atLeastOnce()).save(captor.capture());

        return captor.getAllValues();
    }

    // 프로필별 관리자 생성 테스트

    /**
     * 관리자 생성 옵션이 활성화되어 있어도
     * local 프로필만 단독으로 활성화된 경우에만 관리자 계정이 생성되는지 검증

     * local      : 관리자 생성 가능
     * dev        : 관리자 생성 불가
     * local, dev : 관리자 생성 불가
     */

    @ParameterizedTest
    @CsvSource({
            "local,true",
            "dev,false",
            "'local,dev',false"
    })
    void 관리자_옵션을_켜도_local_단독일_때만_관리자가_생성된다(String profiles, boolean adminExpected) {

        // 전달받은 프로필을 적용하여 시더 실행
        seeder(PASSWORD, true, profiles.split(",")).run(new DefaultApplicationArguments());

        // 저장 요청된 계정 중 ADMIN 역할이 존재하는지 확인
        // 기대 결과와 실제 결과가 일치하는지 검증
        assertThat(savedUsers().stream().anyMatch(u -> u.getRole() == Role.ADMIN))
                .isEqualTo(adminExpected);
    }

    /**
     * 관리자 생성 옵션이 비활성화되어 있으면
     * local 프로필에서도 관리자 계정이 생성되지 않는지 검증
     */

    @Test
    void 관리자_옵션이_꺼져_있으면_local에서도_관리자가_생성되지_않는다() {

        // local 프로필이지만 관리자 생성 옵션은 false로 설정
        seeder(PASSWORD, false, "local").run(new DefaultApplicationArguments());

        // 저장된 계정 중 ADMIN 역할이 없는지 검증
        assertThat(savedUsers()).noneMatch(u -> u.getRole() == Role.ADMIN);
    }

    // 로케일 테스트

    /**
     * 기본 로케일이 터키어인 환경에서도
     * 계정 이메일 접두사가 정상적으로 생성되는지 검증

     * 일부 문자열 대소문자 변환은 로케일에 영향을 받으므로
     * 특정 로케일에 관계없이 동일한 이메일이 생성되는지 확인
     */

    @Test
    void 터키어_로케일에서도_이메일_접두사가_동일하다() {

        // 테스트 실행 전 시스템의 기본 로케일 저장
        Locale original = Locale.getDefault();

        try {
            // 기본 로케일을 터키어로 변경
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            // local 프로필에서 관리자 계정 생성 옵션을 활성화하여 실행
            seeder(PASSWORD, true, "local").run(new DefaultApplicationArguments());
        } finally {
            // 다른 테스트에 영향을 주지 않도록 원래 로케일 복구
            Locale.setDefault(original);
        }

        // 생성된 계정 이메일에 예상한 접두사가 포함되어 있는지 확인
        assertThat(savedUsers()).extracting(User::getEmail)
                .contains("admin1@test.com", "general1@test.com", "vulnerable1@test.com");
    }

    // 비밀번호 검증 테스트

    /**
     * 생성이 거부되어야 하는 비밀번호 목록을 제공

     * 빈 값, 공백만 있는 값, 최소 길이 미달,
     * UTF-8 기준 72바이트 초과 비밀번호를 포함
     */

    static Stream<String> invalidPasswords() {
        return Stream.of(
                "",                 // 빈 값
                " ".repeat(12),     // 공백만 있는 값
                "a".repeat(11),     // 최소 길이인 12자 미만
                "a".repeat(73),     // UTF-8 기준 72바이트 초과 (영문)
                "가".repeat(25));   // UTF-8 기준 75바이트 (한글 25자)
    }

    /**
     * 생성이 허용되어야 하는 비밀번호 목록을 제공

     * 최소 길이와 UTF-8 기준 최대 바이트 경계값을 검증
     */

    static Stream<String> validPasswords() {
        return Stream.of(
                "a".repeat(12),     // 최소 길이 경계
                "a".repeat(72),     // 72바이트 경계 (영문)
                "가".repeat(12),    // 36바이트 (한글)
                "가".repeat(24));   // 72바이트 경계 (한글)
    }

    /**
     * 유효하지 않은 비밀번호를 전달하면
     * 시더 생성 과정에서 IllegalStateException이 발생하는지 검증
     */

    @ParameterizedTest
    @MethodSource("invalidPasswords")
    void 잘못된_비밀번호는_시작_시_설정_오류로_거부된다(String password) {

        // 잘못된 비밀번호를 전달했을 때 설정 오류가 발생하는지 확인
        assertThatThrownBy(() -> seeder(password, false, "local"))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * 유효한 비밀번호를 전달하면
     * 시더 객체 생성 과정에서 예외가 발생하지 않는지 검증

     * 이 테스트는 시더의 생성 가능 여부만 확인하며 run() 실행까지 검증하지는 않는다.
     */

    @ParameterizedTest
    @MethodSource("validPasswords")
    void 경계값_비밀번호는_허용된다(String password) {

        // 유효한 비밀번호로 시더를 생성했을 때 예외가 발생하지 않는지 확인
        assertThatCode(() -> seeder(password, false, "local"))
                .doesNotThrowAnyException();
    }

    // 예외 처리 테스트

    /**
     * 이메일 UNIQUE 제약 조건 위반이 발생했더라도
     * 해당 이메일의 계정이 실제로 존재한다면 정상적인 중복 상황으로 처리하는지 검증
     */

    @Test
    void 이메일_유니크_위반이고_계정이_존재하면_정상_중복으로_처리한다() {

        // 첫 번째 존재 확인에서는 계정이 없고,
        // 저장 충돌 이후 재확인에서는 계정이 존재한다고 응답하도록 설정
        when(repository.existsByEmail("test")).thenReturn(false, true);

        // save() 실행 시 이메일 UNIQUE 제약 조건 위반 상황을 재현
        when(repository.save(any(User.class))).thenThrow(new DataIntegrityViolationException(
                "Duplicate entry 'test' for key 'users.uk_users_email'"));

        // 중복 계정이 실제로 존재하는 경우 예외가 정상적으로 처리되는지 확인
        assertThatCode(() -> seeder(PASSWORD, false, "local").run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();
    }

    /**
     * 이메일 UNIQUE 제약 조건 위반이 아닌 일반적인 DB 오류는
     * 시더 내부에서 임의로 무시하지 않고 호출자에게 전달하는지 검증한다.
     */
    @Test
    void 이메일_유니크_위반이_아닌_DB_오류는_그대로_전파한다() {

        // name 컬럼의 NOT NULL 제약 조건 위반 상황을 재현
        when(repository.save(any(User.class))).thenThrow(
                new DataIntegrityViolationException("Column 'name' cannot be null"));

        // 이메일 중복이 아닌 DB 오류가 그대로 발생하는지 확인
        assertThatThrownBy(() -> seeder(PASSWORD, false, "local").run(new DefaultApplicationArguments()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * 이메일 UNIQUE 제약 조건 위반 메시지가 발생했더라도
     * 실제 계정이 존재하지 않는다면 예외를 무시하지 않는지 검증
     */
    @Test
    void 이메일_제약_메시지여도_계정이_존재하지_않으면_전파한다() {

        // 모든 이메일 존재 확인 결과를 false로 설정
        when(repository.existsByEmail(any())).thenReturn(false);

        // 이메일 UNIQUE 제약 조건 위반 메시지를 포함한 예외 발생
        when(repository.save(any(User.class))).thenThrow(new DataIntegrityViolationException(
                "Duplicate entry 'test' for key 'users.uk_users_email'"));

        // 실제 계정이 존재하지 않으므로 예외가 그대로 전달되는지 확인
        assertThatThrownBy(() -> seeder(PASSWORD, false, "local").run(new DefaultApplicationArguments()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
