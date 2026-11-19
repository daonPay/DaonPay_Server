package com.daon.daonpay.domain.qr.dto.response;

import java.time.LocalDateTime;

public record QrIssueResponse(
        String qrToken,
        LocalDateTime expiresAt,
        long ttlSeconds
) {
}