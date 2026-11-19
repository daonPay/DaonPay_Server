package com.daon.daonpay.domain.qr.service;

import com.daon.daonpay.domain.qr.dto.request.QrVerifyRequest;
import com.daon.daonpay.domain.qr.dto.response.QrVerifyResponse;
import com.daon.daonpay.domain.qr.entity.QrVerification;
import com.daon.daonpay.domain.qr.repository.QrVerificationRepository;
import com.daon.daonpay.domain.terminal.entity.Terminal;
import com.daon.daonpay.domain.terminal.service.TerminalService;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QrVerifyService {

    private final TerminalService terminalService;
    private final QrVerifyProcessor processor;
    private final QrVerificationRepository verificationRepository;

    // 단말기 인증 → QR 검증. 트랜잭션은 processor 안에서만 열림
    public QrVerifyResponse verify(String terminalCode, String terminalSecret, QrVerifyRequest request) {
        Terminal terminal = terminalService.authenticate(terminalCode, terminalSecret);
        try {
            return QrVerifyResponse.from(processor.process(terminal, request));
        } catch (DataIntegrityViolationException e) {
            // 같은 requestId 동시 요청: 먼저 커밋된 결과를 그대로 돌려줌
            return QrVerifyResponse.from(
                    verificationRepository.findByTerminalIdAndRequestId(terminal.getId(), request.requestId())
                            .orElseThrow(() -> e));
        }
    }

    // 응답이 끊긴 단말기가 requestId로 결과를 다시 조회
    public QrVerifyResponse findByRequestId(String terminalCode, String terminalSecret, String requestId) {
        Terminal terminal = terminalService.authenticate(terminalCode, terminalSecret);
        QrVerification verification = verificationRepository
                .findByTerminalIdAndRequestId(terminal.getId(), requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));
        return QrVerifyResponse.from(verification);
    }
}