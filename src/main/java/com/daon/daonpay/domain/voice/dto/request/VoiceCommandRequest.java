package com.daon.daonpay.domain.voice.dto.request;

import com.daon.daonpay.domain.voice.entity.VoiceAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VoiceCommandRequest(
        @NotBlank @Size(max = 200) String transcript,
        @NotNull VoiceAudience audience
) { }
