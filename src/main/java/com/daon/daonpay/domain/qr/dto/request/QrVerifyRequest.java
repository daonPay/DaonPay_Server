package com.daon.daonpay.domain.qr.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//requestId: 단말기가 요청마다 만드는 고유 ID(UUID 권장). 재시도 시 같은 값을 다시 보냄

public record QrVerifyRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 200) String qrToken
) {
}