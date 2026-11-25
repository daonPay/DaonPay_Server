package com.daon.daonpay.domain.whitelist.entity;

import com.daon.daonpay.domain.place.entity.PlaceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * AI가 제안한 이용 가능 장소 상태 변경이다. 제안만 저장하며 관리자 승인 전에는 Place를 변경하지 않는다.
 */
@Entity
@Table(name = "whitelist_change_proposals", indexes = {
        @Index(name = "idx_whitelist_proposals_region", columnList = "region"),
        @Index(name = "idx_whitelist_proposals_status", columnList = "status")
})
public class WhitelistChangeProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long placeId;

    @Column(nullable = false, length = 60)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceStatus suggestedStatus;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false, length = 40)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WhitelistProposalStatus status = WhitelistProposalStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant reviewedAt;

    private Long reviewedBy;

    @Column(length = 500)
    private String reviewComment;

    protected WhitelistChangeProposal() { }

    public WhitelistChangeProposal(Long placeId, String region, PlaceStatus suggestedStatus,
                                   String reason, String provider, Instant createdAt) {
        this.placeId = placeId;
        this.region = region;
        this.suggestedStatus = suggestedStatus;
        this.reason = reason;
        this.provider = provider;
        this.createdAt = createdAt;
    }

    public void review(WhitelistProposalStatus status, Long reviewerId, String comment, Instant reviewedAt) {
        this.status = status;
        this.reviewedBy = reviewerId;
        this.reviewComment = comment;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() { return id; }
    public Long getPlaceId() { return placeId; }
    public String getRegion() { return region; }
    public PlaceStatus getSuggestedStatus() { return suggestedStatus; }
    public String getReason() { return reason; }
    public String getProvider() { return provider; }
    public WhitelistProposalStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public Long getReviewedBy() { return reviewedBy; }
    public String getReviewComment() { return reviewComment; }
}
