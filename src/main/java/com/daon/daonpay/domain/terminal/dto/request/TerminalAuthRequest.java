package com.daon.daonpay.domain.terminal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TerminalAuthRequest(
        @NotBlank String terminalCode,
        @NotBlank String secret
) {
}