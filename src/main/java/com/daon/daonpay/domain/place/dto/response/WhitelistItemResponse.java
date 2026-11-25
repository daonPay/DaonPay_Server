package com.daon.daonpay.domain.place.dto.response;

import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;

/** 이용자 화면에서 사용하는 화이트리스트 항목 표현. */
public record WhitelistItemResponse(
        Long placeId,
        String name,
        PlaceCategory type,
        String typeLabel,
        String region,
        String address
) {
    public static WhitelistItemResponse from(Place place) {
        return new WhitelistItemResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                labelOf(place.getCategory()),
                place.getSido() + " " + place.getSigungu(),
                place.getAddress());
    }

    private static String labelOf(PlaceCategory category) {
        return switch (category) {
            case SHELTER -> "대피소";
            case MERCHANT -> "사용처";
            case PHARMACY -> "약국";
            case ETC -> "기타 사용처";
        };
    }
}
