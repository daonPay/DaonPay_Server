package com.daon.daonpay.domain.qr.dto.response;

import com.daon.daonpay.domain.qr.entity.QrVerification;
import com.daon.daonpay.domain.qr.entity.VerifyResult;
import java.time.LocalDateTime;

public record QrVerifyResponse(
        String requestId,
        Long transactionId,
        boolean approved,
        VerifyResult result,
        String message,
        LocalDateTime verifiedAt
) {
    public static QrVerifyResponse from(QrVerification v) {
        VerifyResult result = v.getResult();
        return new QrVerifyResponse(v.getRequestId(), v.getId(), result.isApproved(),
                result, result.getMessage(), v.getVerifiedAt());
    }
}