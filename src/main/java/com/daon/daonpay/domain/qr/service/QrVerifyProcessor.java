package com.daon.daonpay.domain.qr.service;

import com.daon.daonpay.domain.qr.dto.request.QrVerifyRequest;
import com.daon.daonpay.domain.qr.entity.QrCode;
import com.daon.daonpay.domain.qr.entity.QrStatus;
import com.daon.daonpay.domain.qr.entity.QrVerification;
import com.daon.daonpay.domain.qr.entity.VerifyResult;
import com.daon.daonpay.domain.qr.repository.QrCodeRepository;
import com.daon.daonpay.domain.qr.repository.QrVerificationRepository;
import com.daon.daonpay.domain.terminal.entity.Terminal;
import com.daon.daonpay.global.util.TokenHasher;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 검증 1건을 하나의 트랜잭션으로 처리한다.
 * (QR 소비 + 검증 기록 저장이 함께 커밋되거나 함께 롤백된다)
 */
@Component
@RequiredArgsConstructor
public class QrVerifyProcessor {

    private final QrCodeRepository qrCodeRepository;
    private final QrVerificationRepository verificationRepository;
    private final PlaceAvailabilityChecker placeAvailabilityChecker;

    @Transactional
    public QrVerification process(Terminal terminal, QrVerifyRequest request) {
        Optional<QrVerification> existing =
                verificationRepository.findByTerminalIdAndRequestId(terminal.getId(), request.requestId());
        if (existing.isPresent()) {
            return existing.get();
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<QrCode> found = qrCodeRepository.findByTokenHash(TokenHasher.sha256Hex(request.qrToken().trim()));

        Long qrCodeId = found.map(QrCode::getId).orElse(null);
        VerifyResult result = found.map(qr -> decide(terminal, qr, now)).orElse(VerifyResult.QR_NOT_FOUND);

        // 같은 requestId가 동시에 들어오면 유니크 제약 위반 → 호출부(QrVerifyService)에서 먼저 저장된 결과를 반환
        return verificationRepository.saveAndFlush(
                new QrVerification(terminal.getId(), request.requestId(), qrCodeId, result));
    }

    private VerifyResult decide(Terminal terminal, QrCode qr, LocalDateTime now) {
        if (qr.getStatus() == QrStatus.USED) {
            return VerifyResult.QR_ALREADY_USED;
        }
        if (qr.getStatus() == QrStatus.REVOKED) {
            return VerifyResult.QR_REVOKED;
        }
        if (!qr.getExpiresAt().isAfter(now)) {
            return VerifyResult.QR_EXPIRED;
        }
        // QR을 소비하기 전에 사용처 상태를 확인해, 결제 불가 사용처에서 QR이 소진되지 않게 함
        if (!placeAvailabilityChecker.isAvailable(terminal.getPlaceId())) {
            return VerifyResult.PLACE_UNAVAILABLE;
        }
        // 동시에 두 단말기가 같은 QR을 찍어도 조건부 UPDATE가 성공하는 쪽은 하나뿐
        int updated = qrCodeRepository.markUsed(qr.getId(), terminal.getId(), now);
        return updated == 1 ? VerifyResult.APPROVED : VerifyResult.QR_ALREADY_USED;
    }
}