package com.daon.daonpay.domain.place.dto.response;

import com.daon.daonpay.domain.place.entity.PlaceStatus;
import com.daon.daonpay.domain.place.entity.WhitelistStatusHistory;
import java.time.Instant;

public record WhitelistStatusHistoryResponse(
        Long historyId,
        PlaceStatus previousStatus,
        PlaceStatus changedStatus,
        Instant changedAt
) {
    public static WhitelistStatusHistoryResponse from(WhitelistStatusHistory history) {
        return new WhitelistStatusHistoryResponse(history.getId(), history.getPreviousStatus(),
                history.getChangedStatus(), history.getChangedAt());
    }
}
