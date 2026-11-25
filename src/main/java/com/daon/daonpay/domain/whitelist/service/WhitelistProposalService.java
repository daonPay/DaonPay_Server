package com.daon.daonpay.domain.whitelist.service;

import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceStatus;
import com.daon.daonpay.domain.place.repository.PlaceRepository;
import com.daon.daonpay.domain.place.service.PlaceService;
import com.daon.daonpay.domain.whitelist.ai.WhitelistDecision;
import com.daon.daonpay.domain.whitelist.ai.WhitelistDecisionProvider;
import com.daon.daonpay.domain.whitelist.dto.request.WhitelistProposalCreateRequest;
import com.daon.daonpay.domain.whitelist.dto.request.WhitelistProposalReviewRequest;
import com.daon.daonpay.domain.whitelist.dto.response.WhitelistAiAuditResponse;
import com.daon.daonpay.domain.whitelist.dto.response.WhitelistProposalResponse;
import com.daon.daonpay.domain.whitelist.entity.WhitelistAiAudit;
import com.daon.daonpay.domain.whitelist.entity.WhitelistChangeProposal;
import com.daon.daonpay.domain.whitelist.entity.WhitelistProposalStatus;
import com.daon.daonpay.domain.whitelist.repository.WhitelistAiAuditRepository;
import com.daon.daonpay.domain.whitelist.repository.WhitelistChangeProposalRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WhitelistProposalService {

    private final PlaceRepository placeRepository;
    private final PlaceService placeService;
    private final WhitelistDecisionProvider decisionProvider;
    private final WhitelistChangeProposalRepository proposalRepository;
    private final WhitelistAiAuditRepository auditRepository;

    public WhitelistProposalService(PlaceRepository placeRepository, PlaceService placeService,
                                    WhitelistDecisionProvider decisionProvider,
                                    WhitelistChangeProposalRepository proposalRepository,
                                    WhitelistAiAuditRepository auditRepository) {
        this.placeRepository = placeRepository;
        this.placeService = placeService;
        this.decisionProvider = decisionProvider;
        this.proposalRepository = proposalRepository;
        this.auditRepository = auditRepository;
    }

    /** 지역의 대피소·사용처별 제안을 만들되 실제 운영 상태는 바꾸지 않는다. */
    @Transactional
    public List<WhitelistProposalResponse> create(WhitelistProposalCreateRequest request) {
        String region = request.region().trim();
        String situation = request.disasterSituation().trim();
        List<Place> places = placeRepository.search(null, region, null, null, null, Pageable.unpaged()).getContent();
        return places.stream().map(place -> {
            WhitelistDecision decision = decisionProvider.decide(place, situation);
            WhitelistChangeProposal proposal = proposalRepository.save(new WhitelistChangeProposal(
                    place.getId(), region, decision.suggestedStatus(), decision.reason(), decisionProvider.name(), Instant.now()));
            auditRepository.save(new WhitelistAiAudit(proposal.getId(), decisionProvider.name(),
                    summarize("region=" + region + ", situation=" + situation),
                    summarize("placeId=" + place.getId() + ", status=" + decision.suggestedStatus()
                            + ", reason=" + decision.reason()), Instant.now()));
            return WhitelistProposalResponse.from(proposal);
        }).toList();
    }

    public List<WhitelistProposalResponse> find(WhitelistProposalStatus status) {
        List<WhitelistChangeProposal> proposals = status == null
                ? proposalRepository.findAll()
                : proposalRepository.findByStatusOrderByCreatedAtDesc(status);
        return proposals.stream().map(WhitelistProposalResponse::from).toList();
    }

    public List<WhitelistAiAuditResponse> audits(Long proposalId) {
        getProposal(proposalId);
        return auditRepository.findByProposalIdOrderByCreatedAtAsc(proposalId).stream()
                .map(WhitelistAiAuditResponse::from).toList();
    }

    @Transactional
    public WhitelistProposalResponse review(Long proposalId, WhitelistProposalReviewRequest request, Long adminId) {
        WhitelistChangeProposal proposal = getProposal(proposalId);
        if (proposal.getStatus() != WhitelistProposalStatus.PENDING) {
            throw new CustomException(ErrorCode.WHITELIST_PROPOSAL_ALREADY_REVIEWED);
        }
        WhitelistProposalStatus result = request.approved()
                ? WhitelistProposalStatus.APPROVED : WhitelistProposalStatus.REJECTED;
        if (result == WhitelistProposalStatus.APPROVED) {
            placeService.applyApprovedSuggestion(proposal.getPlaceId(), proposal.getSuggestedStatus());
        }
        proposal.review(result, adminId, request.comment(), Instant.now());
        return WhitelistProposalResponse.from(proposal);
    }

    private WhitelistChangeProposal getProposal(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomException(ErrorCode.WHITELIST_PROPOSAL_NOT_FOUND));
    }

    private String summarize(String value) {
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
