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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terminals",
        uniqueConstraints = @UniqueConstraint(name = "uk_terminals_code", columnNames = "terminal_code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_code", nullable = false, length = 20)
    private String terminalCode;

    @Column(nullable = false, length = 50)
    private String name;

    /** 단말기가 설치된 사용처(place) ID. 미지정 가능. */
    @Column(name = "place_id")
    private Long placeId;

    /** 시크릿 원문은 저장하지 않고 SHA-256 해시만 저장한다. */
    @Column(name = "secret_hash", nullable = false, length = 64)
    private String secretHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TerminalStatus status;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Terminal(String terminalCode, String name, Long placeId, String secretHash) {
        this.terminalCode = terminalCode;
        this.name = name;
        this.placeId = placeId;
        this.secretHash = secretHash;
        this.status = TerminalStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public void changeStatus(TerminalStatus status) {
        this.status = status;
    }

    public void markSeen() {
        this.lastSeenAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == TerminalStatus.ACTIVE;
    }
}