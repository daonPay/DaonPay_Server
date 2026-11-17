package com.daon.daonpay.domain.place.dto.request;

import com.daon.daonpay.domain.place.entity.PlaceStatus;
import jakarta.validation.constraints.NotNull;

// 사용처 운영 상태 변경 요청

public record PlaceStatusUpdateRequest(@NotNull PlaceStatus status) {}