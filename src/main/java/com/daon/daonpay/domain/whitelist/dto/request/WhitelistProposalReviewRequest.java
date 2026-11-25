package com.daon.daonpay.domain.whitelist.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WhitelistProposalReviewRequest(
        @NotNull Boolean approved,
        @Size(max = 500) String comment
) { }
