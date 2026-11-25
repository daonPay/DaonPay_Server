package com.daon.daonpay.domain.voice.dto.response;

import com.daon.daonpay.domain.voice.entity.VoiceIntent;

/** TTS에 바로 전달할 안내 문장과 단말기 화면 전환용 의도. 실제 STT/TTS는 단말기 어댑터가 수행한다. */
public record VoiceCommandResponse(VoiceIntent intent, String speech, boolean actionable) { }
