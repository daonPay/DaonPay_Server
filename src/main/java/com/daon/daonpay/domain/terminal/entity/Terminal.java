package com.daon.daonpay.domain.terminal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terminals",
        uniqueConstraints = @UniqueConstraint(name = Terminal.UK_CODE, columnNames = "terminal_code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terminal {

    public static final String UK_CODE = "uk_terminals_code";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_code", nullable = false, length = 20)
    private String terminalCode;

    @Column(nullable = false, length = 50)
    private String name;

    /** 단말기가 설치된 사용처(place) ID. 미지정이면 결제 승인이 되지 않는다. */
    @Column(name = "place_id")
    private Long placeId;

    /** 시크릿 원문은 저장하지 않고 SHA-256 해시만 저장한다. */
    @Column(name = "secret_hash", nullable = false, length = 64)
    private String secretHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TerminalStatus status;

    // 접속 시각은 TerminalRepository.touchLastSeen 쿼리로만 갱신 (상태 덮어쓰기 방지)
    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Terminal(String terminalCode, String name, Long placeId, String secretHash, Instant createdAt) {
        this.terminalCode = terminalCode;
        this.name = name;
        this.placeId = placeId;
        this.secretHash = secretHash;
        this.status = TerminalStatus.ACTIVE;
        this.createdAt = createdAt;
    }

    public void changeStatus(TerminalStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return status == TerminalStatus.ACTIVE;
    }
}