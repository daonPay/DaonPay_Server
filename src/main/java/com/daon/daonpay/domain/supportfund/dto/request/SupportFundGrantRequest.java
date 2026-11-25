package com.daon.daonpay.domain.supportfund.dto.request;

import jakarta.validation.constraints.Positive;

/** 원 단위 지급 금액. 소수점 금액은 허용하지 않는다. */
public record SupportFundGrantRequest(@Positive long amount) {
}
