package com.daon.daonpay.domain.qr.repository;

import com.daon.daonpay.domain.qr.entity.QrCode;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {

    /**
     * 검증용: 행 락(FOR UPDATE)으로 읽는다. 재발급(revoke)과 겹쳐도 락을 기다린 뒤
     * 최신 커밋 상태를 읽으므로 REVOKED/USED 같은 실제 사유를 정확히 판정할 수 있음
     */

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from QrCode q where q.tokenHash = :tokenHash")
    Optional<QrCode> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    // 사용자가 새 QR을 발급하면 이전에 발급된 미사용 QR은 모두 무효화
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update QrCode q set q.status = com.daon.daonpay.domain.qr.entity.QrStatus.REVOKED "
            + "where q.userId = :userId and q.status = com.daon.daonpay.domain.qr.entity.QrStatus.ISSUED")
    int revokeIssuedByUserId(@Param("userId") Long userId);
}