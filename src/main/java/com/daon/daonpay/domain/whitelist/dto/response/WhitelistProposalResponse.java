package com.daon.daonpay.domain.whitelist.dto.response;

import com.daon.daonpay.domain.place.entity.PlaceStatus;
import com.daon.daonpay.domain.whitelist.entity.WhitelistChangeProposal;
import com.daon.daonpay.domain.whitelist.entity.WhitelistProposalStatus;
import java.time.Instant;

public record WhitelistProposalResponse(
        Long proposalId, Long placeId, String region, PlaceStatus suggestedStatus,
        String reason, String provider, WhitelistProposalStatus status,
        Instant createdAt, Instant reviewedAt, Long reviewedBy, String reviewComment
) {
    public static WhitelistProposalResponse from(WhitelistChangeProposal proposal) {
        return new WhitelistProposalResponse(proposal.getId(), proposal.getPlaceId(), proposal.getRegion(),
                proposal.getSuggestedStatus(), proposal.getReason(), proposal.getProvider(), proposal.getStatus(),
                proposal.getCreatedAt(), proposal.getReviewedAt(), proposal.getReviewedBy(), proposal.getReviewComment());
    }
}
