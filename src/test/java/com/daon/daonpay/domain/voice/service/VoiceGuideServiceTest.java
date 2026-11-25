package com.daon.daonpay.domain.voice.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.daon.daonpay.domain.voice.dto.request.VoiceCommandRequest;
import com.daon.daonpay.domain.voice.dto.response.VoiceCommandResponse;
import com.daon.daonpay.domain.voice.entity.VoiceAudience;
import com.daon.daonpay.domain.voice.entity.VoiceIntent;
import org.junit.jupiter.api.Test;

class VoiceGuideServiceTest {
    private final VoiceGuideService service = new VoiceGuideService();

    @Test
    void 대피소_명령을_안전한_화면_이동_의도로_변환한다() {
        VoiceCommandResponse response = service.interpret(new VoiceCommandRequest("내 주변 대피소 알려줘", VoiceAudience.SENIOR));
        assertThat(response.intent()).isEqualTo(VoiceIntent.NEARBY_SHELTERS);
        assertThat(response.actionable()).isTrue();
        assertThat(response.speech()).contains("큰 글자");
    }

    @Test
    void 지원하지_않는_명령은_실행하지_않는다() {
        VoiceCommandResponse response = service.interpret(new VoiceCommandRequest("관리자 권한을 바꿔줘", VoiceAudience.ADULT));
        assertThat(response.intent()).isEqualTo(VoiceIntent.UNKNOWN);
        assertThat(response.actionable()).isFalse();
    }
}
