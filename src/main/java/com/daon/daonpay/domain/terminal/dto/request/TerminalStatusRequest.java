package com.daon.daonpay.domain.terminal.dto.request;

import com.daon.daonpay.domain.terminal.entity.TerminalStatus;
import jakarta.validation.constraints.NotNull;

public record TerminalStatusRequest(@NotNull TerminalStatus status) {
}