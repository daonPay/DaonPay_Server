package com.daon.daonpay.domain.voice.service;

import com.daon.daonpay.domain.voice.dto.request.VoiceCommandRequest;
import com.daon.daonpay.domain.voice.dto.response.VoiceCommandResponse;
import com.daon.daonpay.domain.voice.entity.VoiceAudience;
import com.daon.daonpay.domain.voice.entity.VoiceIntent;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * STT 결과를 허용된 읽기/화면 전환 명령으로만 정규화한다.
 * 결제·관리 변경 명령은 이 서비스에 없으므로 음성만으로 실행할 수 없다.
 */
@Service
public class VoiceGuideService {

    public VoiceCommandResponse interpret(VoiceCommandRequest request) {
        VoiceIntent intent = identify(request.transcript());
        return new VoiceCommandResponse(intent, message(intent, request.audience()), intent != VoiceIntent.UNKNOWN);
    }

    private VoiceIntent identify(String transcript) {
        String value = transcript.toLowerCase(Locale.ROOT).replace(" ", "");
        if (value.contains("대피소")) return VoiceIntent.NEARBY_SHELTERS;
        if (value.contains("사용처") || value.contains("이용가능장소") || value.contains("가능한장소")) {
            return VoiceIntent.AVAILABLE_PLACES;
        }
        if (value.contains("qr") || value.contains("큐알")) return VoiceIntent.SHOW_QR;
        if (value.contains("이용내역") || value.contains("사용내역")) return VoiceIntent.USAGE_HISTORY;
        return VoiceIntent.UNKNOWN;
    }

    private String message(VoiceIntent intent, VoiceAudience audience) {
        return switch (audience) {
            case CHILD_TEEN -> childMessage(intent);
            case ADULT -> adultMessage(intent);
            case SENIOR -> seniorMessage(intent);
        };
    }

    private String childMessage(VoiceIntent intent) {
        return switch (intent) {
            case NEARBY_SHELTERS -> "가까운 대피소를 찾아볼게요.";
            case AVAILABLE_PLACES -> "지금 이용할 수 있는 곳을 보여드릴게요.";
            case SHOW_QR -> "QR 코드를 화면에 보여드릴게요.";
            case USAGE_HISTORY -> "이용한 내역을 보여드릴게요.";
            case UNKNOWN -> "잘 못 들었어요. 대피소, 사용처, QR 코드, 이용 내역 중 하나를 말해 주세요.";
        };
    }

    private String adultMessage(VoiceIntent intent) {
        return switch (intent) {
            case NEARBY_SHELTERS -> "현재 위치 주변의 이용 가능한 대피소를 조회합니다.";
            case AVAILABLE_PLACES -> "현재 지역에서 이용 가능한 장소를 조회합니다.";
            case SHOW_QR -> "QR 코드 확인 화면으로 이동합니다.";
            case USAGE_HISTORY -> "이용 내역을 조회합니다.";
            case UNKNOWN -> "명령을 이해하지 못했습니다. 대피소, 사용처, QR 코드 또는 이용 내역을 말씀해 주세요.";
        };
    }

    private String seniorMessage(VoiceIntent intent) {
        return switch (intent) {
            case NEARBY_SHELTERS -> "가까운 대피소를 찾겠습니다. 화면에 큰 글자로 보여드릴게요.";
            case AVAILABLE_PLACES -> "지금 이용할 수 있는 장소를 찾겠습니다. 천천히 목록을 보여드릴게요.";
            case SHOW_QR -> "QR 코드를 확인하는 화면으로 이동합니다. 화면의 QR 코드를 다른 단말기에 보여 주세요.";
            case USAGE_HISTORY -> "이용 내역을 보여드리겠습니다. 최근 내역부터 읽어드릴 수 있습니다.";
            case UNKNOWN -> "말씀을 잘 이해하지 못했습니다. ‘대피소 알려줘’ 또는 ‘QR 코드 보여줘’처럼 말씀해 주세요.";
        };
    }
}
