package com.daon.daonpay.domain.place.dto.request;

import com.daon.daonpay.domain.place.entity.PlaceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 사용처 등록 요청.
 * 문자열 길이는 trim 전 요청 값 기준으로 검증하며, 엔티티 컬럼 길이와 맞춘다.
 */

public record PlaceCreateRequest(
        @NotBlank @Size(max = 100) String placeName,    // Place.name (100)
        @NotNull PlaceCategory category,
        @NotBlank @Size(max = 20) String sido,          // Place.sido (20)
        @NotBlank @Size(max = 30) String sigungu,       // Place.sigungu (30)
        @Size(max = 200) String address,                // Place.address (200), 선택값이라 null 허용

        // 초기 보유 상품명 목록 (선택). 목록 자체는 null 가능, 원소는 null/공백 불가
        List<@NotBlank @Size(max = 100) String> productNames
) {}