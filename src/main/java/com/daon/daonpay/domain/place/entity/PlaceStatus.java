package com.daon.daonpay.domain.place.entity;

/**
 * 사용처 운영 상태.
 * OPEN일 때만 이용 가능한 사용처로 취급한다.
 */

public enum PlaceStatus {
    OPEN,       // 운영 중 (사용 가능)
    SUSPENDED,  // 일시 중단
    CLOSED      // 운영 종료
}