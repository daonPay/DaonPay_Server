package com.daon.daonpay.domain.qr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qr_codes",
        uniqueConstraints = @UniqueConstraint(name = "uk_qr_codes_token_hash", columnNames = "token_hash"),
        indexes = @Index(name = "idx_qr_codes_user_status", columnList = "user_id, status"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** QR 토큰 원문은 저장하지 않고 SHA-256 해시만 저장한다. */
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QrStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "used_terminal_id")
    private Long usedTerminalId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public QrCode(Long userId, String tokenHash, Instant expiresAt, Instant createdAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.status = QrStatus.ISSUED;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    // 행 락을 잡은 상태에서만 호출
    public void use(Instant now, Long terminalId) {
        this.status = QrStatus.USED;
        this.usedAt = now;
        this.usedTerminalId = terminalId;
    }
}