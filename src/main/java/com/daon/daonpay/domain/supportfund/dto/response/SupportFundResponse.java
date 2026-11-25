package com.daon.daonpay.domain.supportfund.dto.response;

import com.daon.daonpay.domain.supportfund.entity.SupportFund;

public record SupportFundResponse(Long userId, long totalGranted, long balance) {

    public static SupportFundResponse from(SupportFund fund) {
        return new SupportFundResponse(fund.getUserId(), fund.getTotalGranted(), fund.getBalance());
    }
}
