package com.daon.daonpay.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 화이트리스트 사용처의 운영 상태 변경을 추적한다. */
@Entity
@Table(name = "whitelist_status_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WhitelistStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 20)
    private PlaceStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "changed_status", nullable = false, length = 20)
    private PlaceStatus changedStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public WhitelistStatusHistory(Long placeId, PlaceStatus previousStatus,
                                  PlaceStatus changedStatus, Instant changedAt) {
        this.placeId = placeId;
        this.previousStatus = previousStatus;
        this.changedStatus = changedStatus;
        this.changedAt = changedAt;
    }
}
