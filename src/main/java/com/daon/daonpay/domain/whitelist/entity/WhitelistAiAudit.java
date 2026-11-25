package com.daon.daonpay.domain.whitelist.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 재현 가능한 AI 판단을 위한 최소 감사 기록. 개인정보나 원문 위치 정보는 저장하지 않는다. */
@Entity
@Table(name = "whitelist_ai_audits")
public class WhitelistAiAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long proposalId;

    @Column(nullable = false, length = 40)
    private String provider;

    @Column(nullable = false, length = 1000)
    private String requestSummary;

    @Column(nullable = false, length = 1000)
    private String responseSummary;

    @Column(nullable = false)
    private Instant createdAt;

    protected WhitelistAiAudit() { }

    public WhitelistAiAudit(Long proposalId, String provider, String requestSummary,
                            String responseSummary, Instant createdAt) {
        this.proposalId = proposalId;
        this.provider = provider;
        this.requestSummary = requestSummary;
        this.responseSummary = responseSummary;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getProposalId() { return proposalId; }
    public String getProvider() { return provider; }
    public String getRequestSummary() { return requestSummary; }
    public String getResponseSummary() { return responseSummary; }
    public Instant getCreatedAt() { return createdAt; }
}
