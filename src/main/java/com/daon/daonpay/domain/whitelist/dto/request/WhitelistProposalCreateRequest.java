package com.daon.daonpay.domain.whitelist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WhitelistProposalCreateRequest(
        @NotBlank @Size(max = 60) String region,
        @NotBlank @Size(max = 1000) String disasterSituation
) { }
