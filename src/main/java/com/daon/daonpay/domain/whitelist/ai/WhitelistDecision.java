package com.daon.daonpay.domain.whitelist.ai;

import com.daon.daonpay.domain.place.entity.PlaceStatus;

/** 특정 장소에 대한 AI 판단의 정규화된 결과. */
public record WhitelistDecision(PlaceStatus suggestedStatus, String reason) { }
