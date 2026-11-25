package com.daon.daonpay.domain.voice.controller;

import com.daon.daonpay.domain.voice.dto.request.VoiceCommandRequest;
import com.daon.daonpay.domain.voice.dto.response.VoiceCommandResponse;
import com.daon.daonpay.domain.voice.service.VoiceGuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
public class VoiceGuideController {
    private final VoiceGuideService voiceGuideService;

    @PostMapping("/commands")
    public VoiceCommandResponse command(@Valid @RequestBody VoiceCommandRequest request) {
        return voiceGuideService.interpret(request);
    }
}
