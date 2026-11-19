package com.daon.daonpay.domain.qr.service;

import org.springframework.stereotype.Component;

/**
 * 단말기가 설치된 사용처가 지금 결제 가능한지 확인하는 지점
 * place 도메인의 "사용 가능 여부" 필드와 연결하면 됨
 * (예: placeRepository.findById(placeId).map(Place::isAvailable).orElse(false))
 * 현재는 항상 가능으로 처리
 */

@Component
public class PlaceAvailabilityChecker {

    public boolean isAvailable(Long placeId) {
        return true;
    }
}