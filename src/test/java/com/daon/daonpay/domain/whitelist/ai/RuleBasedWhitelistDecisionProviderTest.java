package com.daon.daonpay.domain.whitelist.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import com.daon.daonpay.domain.place.entity.PlaceStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuleBasedWhitelistDecisionProviderTest {

    private final RuleBasedWhitelistDecisionProvider provider = new RuleBasedWhitelistDecisionProvider();
    private final Place place = new Place("안전 대피소", PlaceCategory.SHELTER, "경상북도", "경산시", "주소", List.of());

    @Test
    void 위험_신호가_있으면_일시중단을_제안한다() {
        WhitelistDecision decision = provider.decide(place, "집중호우로 침수 위험이 있습니다.");

        assertThat(decision.suggestedStatus()).isEqualTo(PlaceStatus.SUSPENDED);
        assertThat(decision.reason()).contains("위험");
    }

    @Test
    void 위험_신호가_없으면_현재_상태를_유지하도록_제안한다() {
        WhitelistDecision decision = provider.decide(place, "평상시 운영 중입니다.");

        assertThat(decision.suggestedStatus()).isEqualTo(PlaceStatus.OPEN);
    }
}
