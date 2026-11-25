package com.daon.daonpay.domain.terminal.dto.response;

import com.daon.daonpay.domain.terminal.entity.Terminal;
import java.time.Instant;

public record TerminalAuthResponse(
        String terminalCode,
        String name,
        Long placeId,
        Instant serverTime
) {
    public static TerminalAuthResponse from(Terminal t, Instant serverTime) {
        return new TerminalAuthResponse(t.getTerminalCode(), t.getName(), t.getPlaceId(), serverTime);
    }
}