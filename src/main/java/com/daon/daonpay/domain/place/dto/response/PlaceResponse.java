package com.daon.daonpay.domain.place.dto.response;

import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import com.daon.daonpay.domain.place.entity.PlaceStatus;

import java.util.List;

// 사용처 응답
public record PlaceResponse(
        Long placeId,
        String placeName,
        PlaceCategory category,
        String sido,
        String sigungu,
        String address,
        PlaceStatus status,

        // 현재 이용 가능 여부 (status가 OPEN이면 true)
        boolean isActive,
        List<PlaceProductResponse> products
) {
    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getSido(),
                place.getSigungu(),
                place.getAddress(),
                place.getStatus(),
                place.isAvailable(),
                place.getProducts().stream().map(PlaceProductResponse::from).toList()
        );
    }
}