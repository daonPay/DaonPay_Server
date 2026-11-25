package com.daon.daonpay.domain.qr.service;

import com.daon.daonpay.domain.qr.dto.request.QrVerifyRequest;
import com.daon.daonpay.domain.qr.entity.QrCode;
import com.daon.daonpay.domain.qr.entity.QrStatus;
import com.daon.daonpay.domain.qr.entity.QrVerification;
import com.daon.daonpay.domain.qr.entity.VerifyResult;
import com.daon.daonpay.domain.qr.repository.QrCodeRepository;
import com.daon.daonpay.domain.qr.repository.QrVerificationRepository;
import com.daon.daonpay.domain.terminal.entity.Terminal;
import com.daon.daonpay.domain.terminal.service.TerminalService;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import com.daon.daonpay.global.util.TokenHasher;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 검증 1건 = 하나의 트랜잭션
 * 단말기 인증(공유 락) → 같은 요청 확인 → QR 행 락 → 판정 → QR 소비 + 기록 저장이
 * 함께 커밋되거나 함께 롤백
 */

@Component
@RequiredArgsConstructor
public class QrVerifyProcessor {

    private final TerminalService terminalService;
    private final QrCodeRepository qrCodeRepository;
    private final QrVerificationRepository verificationRepository;
    private final PlaceAvailabilityChecker placeAvailabilityChecker;
    private final Clock clock;

    @Transactional
    public QrVerification process(String terminalCode, String terminalSecret, QrVerifyRequest request) {
        Terminal terminal = terminalService.authenticateLocked(terminalCode, terminalSecret);
        String qrTokenHash = TokenHasher.sha256Hex(request.qrToken().trim());

        Optional<QrVerification> existing =
                verificationRepository.findByTerminalIdAndRequestId(terminal.getId(), request.requestId());
        if (existing.isPresent()) {
            QrVerification previous = existing.get();
            if (!previous.isSameQr(qrTokenHash)) {

                // 같은 requestId로 다른 QR: 이전 승인 결과를 새 QR 요청에 붙이지 않음
                throw new CustomException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
            }
            return previous;
        }

        Optional<QrCode> found = qrCodeRepository.findByTokenHashForUpdate(qrTokenHash);
        // 잠금 순서: 단말기 → QR → 사용처
        boolean placeAvailable = found.filter(qr -> qr.getStatus() == QrStatus.ISSUED)
                .map(qr -> placeAvailabilityChecker.isAvailable(terminal.getPlaceId()))
                .orElse(false);
        Instant now = Instant.now(clock);

        Long qrCodeId = found.map(QrCode::getId).orElse(null);
        VerifyResult result = found.map(qr -> decide(terminal, qr, now, placeAvailable)).orElse(VerifyResult.QR_NOT_FOUND);

        // 같은 requestId가 동시에 들어오면 유니크 제약 위반 → QrVerifyService에서 먼저 저장된 결과를 반환
        return verificationRepository.saveAndFlush(
                new QrVerification(terminal.getId(), request.requestId(), qrTokenHash, qrCodeId, result, now));
    }

    // QR 행 락을 잡은 상태라 읽은 상태가 최신이고, 아래 판정 사유가 실제 사유
    private VerifyResult decide(Terminal terminal, QrCode qr, Instant now, boolean placeAvailable) {
        if (qr.getStatus() == QrStatus.USED) {
            return VerifyResult.QR_ALREADY_USED;
        }
        if (qr.getStatus() == QrStatus.REVOKED) {
            return VerifyResult.QR_REVOKED;
        }
        if (qr.isExpired(now)) {
            return VerifyResult.QR_EXPIRED;
        }
        // QR을 소비하기 전에 사용처를 확인한다. 불가면 QR은 미사용 상태로 남는다
        if (!placeAvailable) {
            return VerifyResult.PLACE_UNAVAILABLE;
        }
        qr.use(now, terminal.getId());
        return VerifyResult.APPROVED;
    }
}