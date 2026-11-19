package com.daon.daonpay.domain.qr.repository;

import com.daon.daonpay.domain.qr.entity.QrCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {

    Optional<QrCode> findByTokenHash(String tokenHash);

    // 사용자가 새 QR을 발급하면 이전에 발급된 미사용 QR은 모두 무효화
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update QrCode q set q.status = com.daon.daonpay.domain.qr.entity.QrStatus.REVOKED "
            + "where q.userId = :userId and q.status = com.daon.daonpay.domain.qr.entity.QrStatus.ISSUED")
    int revokeIssuedByUserId(@Param("userId") Long userId);

    // 조건부 UPDATE로 한 번만 사용되도록 보장 반환값 0이면 이미 사용/만료/무효화된 것
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update QrCode q set q.status = com.daon.daonpay.domain.qr.entity.QrStatus.USED, "
            + "q.usedAt = :now, q.usedTerminalId = :terminalId "
            + "where q.id = :id and q.status = com.daon.daonpay.domain.qr.entity.QrStatus.ISSUED "
            + "and q.expiresAt > :now")
    int markUsed(@Param("id") Long id,
                 @Param("terminalId") Long terminalId,
                 @Param("now") LocalDateTime now);
}