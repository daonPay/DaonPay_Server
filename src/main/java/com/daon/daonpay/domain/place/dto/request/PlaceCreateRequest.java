package com.daon.daonpay.domain.place.dto.request;

import com.daon.daonpay.domain.place.entity.PlaceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 사용처 등록 요청
public record PlaceCreateRequest(
        @NotBlank String placeName,
        @NotNull PlaceCategory category,
        @NotBlank String sido,
        @NotBlank String sigungu,
        String address,

        // 초기 보유 상품명 목록 (선택)
        List<String> productNames
) {}