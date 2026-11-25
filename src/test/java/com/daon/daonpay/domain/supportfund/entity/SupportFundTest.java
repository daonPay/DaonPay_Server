package com.daon.daonpay.domain.supportfund.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SupportFundTest {

    @Test
    void grantAddsToBalanceAndTotal() {
        SupportFund fund = new SupportFund(1L);

        fund.grant(30_000);
        fund.grant(20_000);

        assertThat(fund.getTotalGranted()).isEqualTo(50_000);
        assertThat(fund.getBalance()).isEqualTo(50_000);
    }

    @Test
    void grantRejectsNonPositiveAmount() {
        SupportFund fund = new SupportFund(1L);

        assertThatThrownBy(() -> fund.grant(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
