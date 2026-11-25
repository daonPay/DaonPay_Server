package com.daon.daonpay.domain.whitelist.ai;

import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceStatus;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * API 키 없이 시연 가능한 기본 판단기. 실제 LLM 연동은 WhitelistDecisionProvider 구현체로 추가한다.
 * 폐쇄·위험 신호가 있을 때만 상태 중단을 제안해, 불필요한 자동 차단을 피한다.
 */
@Component
public class RuleBasedWhitelistDecisionProvider implements WhitelistDecisionProvider {

    @Override
    public String name() {
        return "RULE_BASED_DEMO";
    }

    @Override
    public WhitelistDecision decide(Place place, String disasterSituation) {
        String situation = disasterSituation.toLowerCase(Locale.ROOT);
        boolean riskSignal = situation.contains("침수") || situation.contains("화재")
                || situation.contains("붕괴") || situation.contains("폐쇄") || situation.contains("통제");
        if (riskSignal && place.isAvailable()) {
            return new WhitelistDecision(PlaceStatus.SUSPENDED,
                    "재난 상황에 위험·통제 신호가 포함되어 현장 확인 전 일시 중단을 제안합니다.");
        }
        return new WhitelistDecision(place.getStatus(),
                "현재 운영 상태와 입력된 재난 상황에서 즉시 변경이 필요한 위험 신호를 찾지 못했습니다.");
    }
}
