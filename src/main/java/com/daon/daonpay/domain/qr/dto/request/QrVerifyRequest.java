package com.daon.daonpay.domain.qr.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// requestId: 단말기가 요청마다 만드는 고유 ID(UUID 권장). 대소문자를 구분하지 않는다.

public record QrVerifyRequest(
        @NotBlank @Pattern(regexp = REQUEST_ID_PATTERN) String requestId,
        @NotBlank @Size(max = 200) String qrToken
) {
    public static final String REQUEST_ID_PATTERN = "[A-Za-z0-9_-]{1,64}";
}