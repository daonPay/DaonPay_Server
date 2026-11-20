package com.daon.daonpay.domain.terminal.repository;

import com.daon.daonpay.domain.terminal.entity.Terminal;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TerminalRepository extends JpaRepository<Terminal, Long> {

    Optional<Terminal> findByTerminalCode(String terminalCode);

    // QR 검증 트랜잭션용: 공유 락. 검증이 끝나기 전에는 상태 변경(쓰기 락)이 반영되지 않음
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select t from Terminal t where t.terminalCode = :code")
    Optional<Terminal> findByTerminalCodeForShare(@Param("code") String code);

    // 상태 변경용: 쓰기 락. 진행 중인 검증이 끝난 뒤 최신 상태를 읽고 변경
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Terminal t where t.id = :id")
    Optional<Terminal> findByIdForUpdate(@Param("id") Long id);

    // 접속 시각 컬럼만 갱신한다. 엔티티 전체 UPDATE가 아니라서 상태를 덮어쓰지 않음
    @Modifying(clearAutomatically = true)
    @Query("update Terminal t set t.lastSeenAt = :now where t.id = :id")
    int touchLastSeen(@Param("id") Long id, @Param("now") Instant now);
}