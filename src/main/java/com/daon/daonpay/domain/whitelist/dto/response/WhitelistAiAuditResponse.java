package com.daon.daonpay.domain.whitelist.dto.response;

import com.daon.daonpay.domain.whitelist.entity.WhitelistAiAudit;
import java.time.Instant;

public record WhitelistAiAuditResponse(Long auditId, String provider, String requestSummary,
                                       String responseSummary, Instant createdAt) {
    public static WhitelistAiAuditResponse from(WhitelistAiAudit audit) {
        return new WhitelistAiAuditResponse(audit.getId(), audit.getProvider(), audit.getRequestSummary(),
                audit.getResponseSummary(), audit.getCreatedAt());
    }
}
