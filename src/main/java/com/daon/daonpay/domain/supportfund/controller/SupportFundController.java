package com.daon.daonpay.domain.supportfund.controller;

import com.daon.daonpay.domain.supportfund.dto.request.SupportFundGrantRequest;
import com.daon.daonpay.domain.supportfund.dto.response.SupportFundResponse;
import com.daon.daonpay.domain.supportfund.service.SupportFundService;
import com.daon.daonpay.global.jwt.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support-funds")
@RequiredArgsConstructor
public class SupportFundController {

    private final SupportFundService supportFundService;

    @GetMapping("/me")
    public SupportFundResponse getMine(@AuthenticationPrincipal AuthUser authUser) {
        return supportFundService.getMine(authUser.userId());
    }

    @PostMapping("/{userId}/grants")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SupportFundResponse grant(
            @PathVariable Long userId, @Valid @RequestBody SupportFundGrantRequest request) {
        return supportFundService.grant(userId, request.amount());
    }
}
