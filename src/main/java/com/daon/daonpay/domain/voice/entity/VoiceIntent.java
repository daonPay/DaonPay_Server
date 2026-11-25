package com.daon.daonpay.domain.voice.entity;

/** 클라이언트가 실제 화면 이동·API 호출로 변환할 수 있는 안전한 음성 명령 목록. */
public enum VoiceIntent {
    NEARBY_SHELTERS,
    AVAILABLE_PLACES,
    SHOW_QR,
    USAGE_HISTORY,
    UNKNOWN
}
