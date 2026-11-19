package com.daon.daonpay.domain.terminal.dto.response;

import com.daon.daonpay.domain.terminal.entity.Terminal;
import com.daon.daonpay.domain.terminal.entity.TerminalStatus;
import java.time.LocalDateTime;

public record TerminalResponse(
        Long id,
        String terminalCode,
        String name,
        Long placeId,
        TerminalStatus status,
        LocalDateTime lastSeenAt
) {
    public static TerminalResponse from(Terminal t) {
        return new TerminalResponse(t.getId(), t.getTerminalCode(), t.getName(),
                t.getPlaceId(), t.getStatus(), t.getLastSeenAt());
    }
}