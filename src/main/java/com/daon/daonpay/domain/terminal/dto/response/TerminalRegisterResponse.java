package com.daon.daonpay.domain.terminal.dto.response;

import com.daon.daonpay.domain.terminal.entity.Terminal;
import com.daon.daonpay.domain.terminal.entity.TerminalStatus;

// 시크릿은 등록 응답에서 단 한 번만 노출
public record TerminalRegisterResponse(
        Long id,
        String terminalCode,
        String name,
        Long placeId,
        TerminalStatus status,
        String secret
) {
    public static TerminalRegisterResponse of(Terminal t, String rawSecret) {
        return new TerminalRegisterResponse(t.getId(), t.getTerminalCode(), t.getName(),
                t.getPlaceId(), t.getStatus(), rawSecret);
    }
}