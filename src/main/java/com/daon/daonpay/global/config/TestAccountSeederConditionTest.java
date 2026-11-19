
package com.daon.daonpay.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * 테스트 계정 시더의 동시 실행 안정성을 검증하는 테스트 클래스

 * 여러 스레드에서 시더를 동시에 실행하더라도
 * 동일한 계정이 중복 생성되지 않는지 확인
 */

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = {

        // 테스트 계정 자동 생성 활성화
        "app.seed.accounts.enabled=true",

        // 테스트 계정에 사용할 비밀번호 설정
        "app.seed.accounts.password=test-password-1234"
})

class TestAccountSeederConcurrencyTest {

    /**
     * 생성 여부를 확인할 일반 사용자 및 취약 사용자 계정 목록
     * 관리자 계정은 이 테스트에서 생성 대상에 포함하지 않음
     */
    private static final List<String> EXPECTED = List.of(
            "test",
            "general1@test.com", "general2@test.com", "general3@test.com",
            "vulnerable1@test.com", "vulnerable2@test.com", "vulnerable3@test.com");

    // 테스트 대상 시더 주입
    @Autowired TestAccountSeeder seeder;

    // 계정 조회 및 삭제를 위한 UserRepository 주입
    @Autowired UserRepository userRepository;

    /**
     * 여러 스레드에서 시더를 동시에 실행해도
     * 각 테스트 계정이 한 건씩만 생성되는지 확인

     * 추가로 관리자 계정이 생성되지 않는지도 검증
     */

    @Test
    void 동시에_실행해도_각_계정은_한_건씩만_생성되고_관리자는_생성되지_않는다() throws Exception {

        // 테스트 시작 전 기존 테스트 계정을 삭제하여 초기 상태 구성
        EXPECTED.forEach(email ->
                userRepository.findByEmail(email).ifPresent(userRepository::delete));

        // 동시에 작업을 수행할 스레드 수
        int threads = 4;

        // 지정한 개수만큼 스레드를 생성하는 스레드 풀
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        // 모든 스레드가 동시에 시작하도록 대기시키는 동기화 도구
        CountDownLatch start = new CountDownLatch(1);

        // 각 스레드의 실행 결과를 저장할 Future 목록
        List<Future<?>> futures = new ArrayList<>();

        // 여러 스레드에서 시더를 동시에 실행
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {

                // 시작 신호가 전달될 때까지 대기
                start.await();

                // 시더 실행
                seeder.run(new DefaultApplicationArguments());

                return null;
            }));
        }

        // 모든 스레드에 동시에 시작 신호 전달
        start.countDown();

        // 모든 스레드의 작업이 완료될 때까지 대기
        for (Future<?> f : futures) {
            f.get(); // 어느 스레드에서든 예외가 발생하면 테스트 실패
        }

        // 작업이 끝난 스레드 풀 종료
        pool.shutdown();

        // DB에서 테스트 대상 계정을 조회하고 예상 목록에 포함된 계정만 추출
        List<User> seeded = userRepository.findAll().stream()
                .filter(u -> EXPECTED.contains(u.getEmail()))
                .toList();

        // 예상한 계정 수만큼만 생성되었는지 확인
        assertThat(seeded).hasSize(EXPECTED.size());

        // 관리자 계정은 생성되지 않았는지 확인
        assertThat(userRepository.existsByEmail("admin1@test.com")).isFalse();
    }
}
