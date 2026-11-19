package com.daon.daonpay.domain.qr.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 단말기에 돌려주는 결제 가능/불가 판정 결과
@Getter
@RequiredArgsConstructor
public enum VerifyResult {
    APPROVED("결제 가능"),
    QR_NOT_FOUND("유효하지 않은 QR입니다"),
    QR_EXPIRED("만료된 QR입니다"),
    QR_ALREADY_USED("이미 사용된 QR입니다"),
    QR_REVOKED("무효화된 QR입니다"),
    PLACE_UNAVAILABLE("현재 결제할 수 없는 사용처입니다");

    private final String message;

    public boolean isApproved() {
        return this == APPROVED;
    }
}