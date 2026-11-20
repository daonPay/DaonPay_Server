package com.daon.daonpay.domain.user.repository;

import com.daon.daonpay.domain.user.entity.User;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일을 기준으로 사용자 조회
    // 사용자가 존재하면 User 반환, 존재하지 않으면 Optional.empty() 반환
    Optional<User> findByEmail(String email);

    // 해당 이메일을 사용하는 사용자가 이미 존재하는지 확인
    // 존재하면 true, 존재하지 않으면 false 반환
    boolean existsByEmail(String email);

    // QR 발급 직렬화용: 사용자 행에 쓰기 락을 잡고 조회 (JWT 의 userId 기준)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}