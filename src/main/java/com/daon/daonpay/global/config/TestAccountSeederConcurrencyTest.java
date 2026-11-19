package com.daon.daonpay.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;

class TestAccountSeederConcurrencyTest {

    // 동시에 시더를 실행할 스레드 수
    private static final int THREADS = 4;

    // 시더 실행 후 생성되어야 하는 계정 이메일 목록
    // 관리자 계정은 생성 대상에 포함하지 않음
    private static final List<String> EXPECTED = List.of(
            "test",
            "general1@test.com", "general2@test.com", "general3@test.com",
            "vulnerable1@test.com", "vulnerable2@test.com", "vulnerable3@test.com");

    @Test
    void 동시에_실행해도_각_계정은_한_건씩만_저장되고_관리자는_생성되지_않는다() throws Exception {

        // 실제 DB 대신 계정 정보를 저장할 동시성 안전한 임시 저장소
        ConcurrentMap<String, User> store = new ConcurrentHashMap<>();

        // 이메일별로 스레드들이 동시에 도착했는지 확인하기 위한 동기화 장치
        ConcurrentMap<String, CyclicBarrier> barriers = new ConcurrentHashMap<>();

        // 각 스레드가 이메일별 존재 여부를 처음 확인했는지 기록
        // 같은 스레드의 재확인에서는 다시 대기하지 않도록 사용
        ThreadLocal<Set<String>> checked = ThreadLocal.withInitial(HashSet::new);

        // 중복 저장 시도 횟수를 집계하는 카운터
        AtomicInteger conflicts = new AtomicInteger();

        // 실제 DB 접근 대신 Mockito로 가짜 UserRepository 생성
        UserRepository repository = mock(UserRepository.class);

        // 이메일 존재 여부 확인 동작 정의
        when(repository.existsByEmail(anyString())).thenAnswer(inv -> {
            String email = inv.getArgument(0);

            // 현재 저장소에 해당 이메일이 존재하는지 확인
            boolean exists = store.containsKey(email);

            // 각 스레드가 해당 이메일을 처음 확인하는 경우에만 대기
            // 모든 스레드가 존재 여부를 확인할 때까지 기다려
            // 동시에 저장 단계에 진입하는 상황을 재현
            if (checked.get().add(email)) {
                barriers.computeIfAbsent(email, e -> new CyclicBarrier(THREADS))
                        .await(5, TimeUnit.SECONDS);
            }

            return exists;
        });

        // 계정 저장 동작 정의
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);

            // putIfAbsent를 사용해 동일 이메일의 중복 저장을 방지
            // 이미 존재하면 DB의 UNIQUE 제약 조건 위반 상황을 흉내 냄
            if (store.putIfAbsent(user.getEmail(), user) != null) {
                conflicts.incrementAndGet();

                throw new DataIntegrityViolationException(
                        "Duplicate entry '" + user.getEmail() + "' for key 'users.uk_users_email'");
            }

            return user;
        });

        // 비밀번호 암호화 기능을 대체할 가짜 PasswordEncoder
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        // 암호화 결과를 항상 동일한 문자열로 반환하도록 설정
        when(encoder.encode(any())).thenReturn("encoded");

        // local 프로필을 활성화한 테스트 환경 구성
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("local");

        // 실제 시더 객체 생성
        // 관리자 계정 생성 옵션은 false로 설정
        TestAccountSeeder seeder = new TestAccountSeeder(
                repository, encoder, env, "test-password-1234", false);

        // 여러 스레드에서 동시에 시더를 실행하기 위한 스레드 풀
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        // 각 스레드의 실행 결과를 확인하기 위한 Future 목록
        List<Future<?>> futures = new ArrayList<>();

        try {
            // 4개의 스레드에서 시더를 동시에 실행
            for (int i = 0; i < THREADS; i++) {
                futures.add(pool.submit(
                        () -> seeder.run(new DefaultApplicationArguments())));
            }

            // 모든 작업이 제한 시간 안에 정상적으로 끝나는지 확인
            // 실행 중 예외가 발생하면 future.get()에서 예외가 전달됨
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            // 테스트 종료 시 남아 있는 작업을 취소하고 스레드 풀 종료
            futures.forEach(f -> f.cancel(true));
            pool.shutdownNow();
        }

        // 최종 저장된 계정이 예상한 목록과 정확히 일치하는지 검증
        assertThat(store.keySet())
                .containsExactlyInAnyOrderElementsOf(EXPECTED);

        // 관리자 계정이 생성되지 않았는지 검증
        assertThat(store.values())
                .noneMatch(u -> u.getRole() == Role.ADMIN);

        // 중복 삽입 경로를 실제로 거쳤는지 확인
        // 이메일마다 최초 저장 1건을 제외한 나머지 스레드가 중복 충돌해야 함
        assertThat(conflicts.get())
                .isEqualTo((THREADS - 1) * EXPECTED.size());
    }
}
