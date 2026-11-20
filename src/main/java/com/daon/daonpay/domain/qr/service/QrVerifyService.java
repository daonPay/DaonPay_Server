package com.daon.daonpay.domain.qr.service;

import com.daon.daonpay.domain.qr.dto.request.QrVerifyRequest;
import com.daon.daonpay.domain.qr.dto.response.QrVerifyResponse;
import com.daon.daonpay.domain.qr.entity.QrVerification;
import com.daon.daonpay.domain.qr.repository.QrVerificationRepository;
import com.daon.daonpay.domain.terminal.entity.Terminal;
import com.daon.daonpay.domain.terminal.service.TerminalService;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import com.daon.daonpay.global.util.TokenHasher;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QrVerifyService {

    private final TerminalService terminalService;
    private final QrVerifyProcessor processor;
    private final QrVerificationRepository verificationRepository;

    // 트랜잭션은 processor 안에서만 열림 (단말기 인증 → 판정 → 기록이 한 트랜잭션)
    public QrVerifyResponse verify(String terminalCode, String terminalSecret, QrVerifyRequest request) {
        try {
            QrVerification verification = processor.process(terminalCode, terminalSecret, request);
            terminalService.touchLastSeen(verification.getTerminalId());
            return QrVerifyResponse.from(verification);
        } catch (DataIntegrityViolationException e) {
            if (!isRequestIdConflict(e)) {
                throw e; // 다른 무결성 오류는 재시도로 보지 않고 그대로 전파
            }
            return QrVerifyResponse.from(findCommitted(terminalCode, terminalSecret, request, e));
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

    // 먼저 커밋된 같은 requestId의 결과를 읽음. 요청 내용(QR)이 다르면 충돌로 응답
    private QrVerification findCommitted(String terminalCode, String terminalSecret,
                                         QrVerifyRequest request, DataIntegrityViolationException cause) {
        Terminal terminal = terminalService.authenticate(terminalCode, terminalSecret);
        QrVerification existing = verificationRepository
                .findByTerminalIdAndRequestId(terminal.getId(), request.requestId())
                .orElseThrow(() -> cause);
        if (!existing.isSameQr(TokenHasher.sha256Hex(request.qrToken().trim()))) {
            throw new CustomException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }
        return existing;
    }

    // (terminal_id, request_id) 유니크 제약 위반인지 확인
    private boolean isRequestIdConflict(DataIntegrityViolationException e) {
        String constraint = QrVerification.UK_TERMINAL_REQUEST.toLowerCase(Locale.ROOT);
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve && cve.getConstraintName() != null
                    && cve.getConstraintName().toLowerCase(Locale.ROOT).contains(constraint)) {
                return true;
            }
            if (t.getMessage() != null && t.getMessage().toLowerCase(Locale.ROOT).contains(constraint)) {
                return true;
            }
        }
        return false;
    }
}