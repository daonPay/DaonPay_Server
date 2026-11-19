package com.daon.daonpay.domain.terminal.dto.response;

import com.daon.daonpay.domain.terminal.entity.Terminal;
import java.time.LocalDateTime;

public record TerminalAuthResponse(
        String terminalCode,
        String name,
        Long placeId,
        LocalDateTime serverTime
) {
    public static TerminalAuthResponse from(Terminal t) {
        return new TerminalAuthResponse(t.getTerminalCode(), t.getName(), t.getPlaceId(), LocalDateTime.now());
    }
}