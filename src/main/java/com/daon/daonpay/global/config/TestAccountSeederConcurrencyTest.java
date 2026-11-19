
package com.daon.daonpay.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.daon.daonpay.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 테스트 계정 시더(TestAccountSeeder)의 등록 조건을 검증하는 테스트 클래스
 *
 * local 프로필과 계정 생성 활성화 옵션이 모두 설정된 경우에만
 * 시더가 스프링 컨테이너에 등록되는지 확인한다.
 */
class TestAccountSeederConditionTest {

    /**
     * 테스트용 스프링 애플리케이션 컨텍스트 생성기
     *
     * 실제 DB나 비밀번호 암호화 기능을 사용하지 않도록
     * UserRepository와 PasswordEncoder를 Mock 객체로 등록한다.
     */
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestAccountSeeder.class)
            .withBean(UserRepository.class, () -> mock(UserRepository.class))
            .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class));

    // 테스트 계정 자동 생성 활성화 설정
    private static final String ENABLED = "app.seed.accounts.enabled=true";

    // 테스트 계정에 사용할 비밀번호 설정 (12자 이상)
    private static final String PASSWORD = "app.seed.accounts.password=test-password-1234";

    /**
     * 기본 실행 환경에서는 테스트 계정 시더가 등록되지 않는지 확인한다.
     */
    @Test
    void 기본_실행에서는_시드가_등록되지_않는다() {
        runner.run(ctx -> assertThat(ctx).doesNotHaveBean(TestAccountSeeder.class));
    }

    /**
     * local 프로필만 활성화하고 계정 생성 옵션을 설정하지 않으면
     * 시더가 등록되지 않는지 확인한다.
     */
    @Test
    void local_프로필만_있고_옵션이_없으면_등록되지_않는다() {
        runner.withInitializer(c -> c.getEnvironment().setActiveProfiles("local"))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(TestAccountSeeder.class));
    }

    /**
     * 계정 생성 옵션만 활성화하고 local 프로필을 설정하지 않으면
     * 시더가 등록되지 않는지 확인한다.
     */
    @Test
    void 옵션만_있고_프로필이_없으면_등록되지_않는다() {
        runner.withPropertyValues(ENABLED, PASSWORD)
                .run(ctx -> assertThat(ctx).doesNotHaveBean(TestAccountSeeder.class));
    }

    /**
     * local 프로필과 계정 생성 옵션이 모두 설정된 경우
     * 시더가 정상적으로 스프링 컨테이너에 등록되는지 확인한다.
     */
    @Test
    void 프로필과_옵션이_모두_있으면_등록된다() {
        runner.withInitializer(c -> c.getEnvironment().setActiveProfiles("local"))
                .withPropertyValues(ENABLED, PASSWORD)
                .run(ctx -> assertThat(ctx).hasSingleBean(TestAccountSeeder.class));
    }

    /**
     * 계정 생성이 활성화되었지만 비밀번호가 설정되지 않은 경우
     * 필수 설정 누락으로 애플리케이션 컨텍스트 시작이 실패하는지 확인한다.
     */
    @Test
    void 비밀번호가_없으면_시작이_실패한다() {
        runner.withInitializer(c -> c.getEnvironment().setActiveProfiles("local"))
                .withPropertyValues(ENABLED)
                .run(ctx -> assertThat(ctx).hasFailed());
    }
}
