package com.daon.daonpay.domain.qr.dto.response;

import java.time.Instant;

public record QrIssueResponse(
        String qrToken,
        Instant expiresAt,
        long ttlSeconds
) {
}