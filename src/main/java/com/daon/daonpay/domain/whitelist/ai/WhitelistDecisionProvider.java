package com.daon.daonpay.domain.whitelist.ai;

import com.daon.daonpay.domain.place.entity.Place;

/** 외부 LLM 또는 규칙 기반 시연기를 교체해 연결할 수 있는 판단 경계. */
public interface WhitelistDecisionProvider {
    String name();
    WhitelistDecision decide(Place place, String disasterSituation);
}
