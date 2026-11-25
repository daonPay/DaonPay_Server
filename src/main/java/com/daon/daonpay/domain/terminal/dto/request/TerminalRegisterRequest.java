package com.daon.daonpay.domain.terminal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TerminalRegisterRequest(
        @NotBlank @Size(max = 50) String name,
        @Positive Long placeId
) {
}