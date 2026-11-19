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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 단말기의 QR 검증 요청 기록. (terminal_id, request_id)가 유니크라서
 * 응답이 끊겨 단말기가 같은 requestId로 재요청해도 같은 결과를 돌려주고 중복 처리되지 않는다.
 */
@Entity
@Table(name = "qr_verifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_qr_verifications_terminal_request",
                columnNames = {"terminal_id", "request_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QrVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_id", nullable = false)
    private Long terminalId;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "qr_code_id")
    private Long qrCodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerifyResult result;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    public QrVerification(Long terminalId, String requestId, Long qrCodeId, VerifyResult result) {
        this.terminalId = terminalId;
        this.requestId = requestId;
        this.qrCodeId = qrCodeId;
        this.result = result;
        this.verifiedAt = LocalDateTime.now();
    }
}