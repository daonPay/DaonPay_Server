package com.daon.daonpay.domain.whitelist.controller;

import com.daon.daonpay.domain.whitelist.dto.request.WhitelistProposalCreateRequest;
import com.daon.daonpay.domain.whitelist.dto.request.WhitelistProposalReviewRequest;
import com.daon.daonpay.domain.whitelist.dto.response.WhitelistAiAuditResponse;
import com.daon.daonpay.domain.whitelist.dto.response.WhitelistProposalResponse;
import com.daon.daonpay.domain.whitelist.entity.WhitelistProposalStatus;
import com.daon.daonpay.domain.whitelist.service.WhitelistProposalService;
import com.daon.daonpay.global.jwt.AuthUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 전용 AI 판단·검토 API. SecurityConfig의 /api/admin/** 규칙으로 보호된다. */
@RestController
@RequestMapping("/api/admin/whitelist-proposals")
@RequiredArgsConstructor
public class WhitelistProposalAdminController {

    private final WhitelistProposalService proposalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<WhitelistProposalResponse> create(@Valid @RequestBody WhitelistProposalCreateRequest request) {
        return proposalService.create(request);
    }

    @GetMapping
    public List<WhitelistProposalResponse> find(@RequestParam(required = false) WhitelistProposalStatus status) {
        return proposalService.find(status);
    }

    @GetMapping("/{proposalId}/audits")
    public List<WhitelistAiAuditResponse> audits(@PathVariable Long proposalId) {
        return proposalService.audits(proposalId);
    }

    @PatchMapping("/{proposalId}/review")
    public WhitelistProposalResponse review(@PathVariable Long proposalId,
                                            @Valid @RequestBody WhitelistProposalReviewRequest request,
                                            @AuthenticationPrincipal AuthUser authUser) {
        return proposalService.review(proposalId, request, authUser.userId());
    }
}
