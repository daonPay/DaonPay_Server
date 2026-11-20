package com.daon.daonpay.domain.qr.entity;

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

/**
 * 단말기의 QR 검증 요청 기록. (terminal_id, request_id)가 유니크라서
 * 응답이 끊겨 단말기가 같은 requestId로 재요청해도 같은 결과를 돌려주고 중복 처리되지 않음
 * qr_token_hash로 "같은 requestId에 다른 QR"이 온 경우를 구분
 */

@Entity
@Table(name = "qr_verifications",
        uniqueConstraints = @UniqueConstraint(
                name = QrVerification.UK_TERMINAL_REQUEST,
                columnNames = {"terminal_id", "request_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QrVerification {

    public static final String UK_TERMINAL_REQUEST = "uk_qr_verifications_terminal_request";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_id", nullable = false)
    private Long terminalId;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    // 정규화(trim)한 QR 토큰의 SHA-256 요청 내용 비교용
    @Column(name = "qr_token_hash", nullable = false, length = 64)
    private String qrTokenHash;

    @Column(name = "qr_code_id")
    private Long qrCodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerifyResult result;

    @Column(name = "verified_at", nullable = false)
    private Instant verifiedAt;

    public QrVerification(Long terminalId, String requestId, String qrTokenHash,
                          Long qrCodeId, VerifyResult result, Instant verifiedAt) {
        this.terminalId = terminalId;
        this.requestId = requestId;
        this.qrTokenHash = qrTokenHash;
        this.qrCodeId = qrCodeId;
        this.result = result;
        this.verifiedAt = verifiedAt;
    }

    public boolean isSameQr(String qrTokenHash) {
        return this.qrTokenHash.equals(qrTokenHash);
    }
}