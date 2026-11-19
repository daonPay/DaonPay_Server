package com.daon.daonpay.domain.qr.controller;

import com.daon.daonpay.domain.qr.dto.request.QrVerifyRequest;
import com.daon.daonpay.domain.qr.dto.response.QrIssueResponse;
import com.daon.daonpay.domain.qr.dto.response.QrVerifyResponse;
import com.daon.daonpay.domain.qr.service.QrIssueService;
import com.daon.daonpay.domain.qr.service.QrVerifyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {

    private static final String TERMINAL_CODE = "X-Terminal-Code";
    private static final String TERMINAL_SECRET = "X-Terminal-Secret";

    private final QrIssueService qrIssueService;
    private final QrVerifyService qrVerifyService;

    // 로그인한 사용자: 결제용 QR 발급 (앱 카드 화면의 큰 QR)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QrIssueResponse issue(Authentication authentication) {
        return qrIssueService.issue(authentication.getName());
    }

    // 단말기: QR 검증 → 결제 가능/불가 응답 (공개 경로, 단말기 헤더로 인증)
    @PostMapping("/verify")
    public QrVerifyResponse verify(
            @RequestHeader(value = TERMINAL_CODE, required = false) String terminalCode,
            @RequestHeader(value = TERMINAL_SECRET, required = false) String terminalSecret,
            @Valid @RequestBody QrVerifyRequest request) {
        return qrVerifyService.verify(terminalCode, terminalSecret, request);
    }

    // 단말기: 응답이 끊겼을 때 requestId로 검증 결과 재조회
    @GetMapping("/verifications/{requestId}")
    public QrVerifyResponse findVerification(
            @RequestHeader(value = TERMINAL_CODE, required = false) String terminalCode,
            @RequestHeader(value = TERMINAL_SECRET, required = false) String terminalSecret,
            @PathVariable String requestId) {
        return qrVerifyService.findByRequestId(terminalCode, terminalSecret, requestId);
    }
}