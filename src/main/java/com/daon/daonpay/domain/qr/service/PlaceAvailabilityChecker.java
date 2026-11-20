package com.daon.daonpay.domain.qr.service;

import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 단말기가 설치된 사용처가 지금 결제 가능한지 확인
 * 사용처 미연결, 존재하지 않음, 운영 중단은 모두 불가로 처리한다(fail-closed).
 */

@Component
@RequiredArgsConstructor
public class PlaceAvailabilityChecker {

    private final PlaceRepository placeRepository;

    public boolean isAvailable(Long placeId) {
        if (placeId == null) {
            return false;
        }
        // Place.isAvailable(): status == OPEN 일 때만 true
        return placeRepository.findById(placeId)
                .map(Place::isAvailable)
                .orElse(false);
    }
}