package com.daon.daonpay.domain.qr.service;

import com.daon.daonpay.domain.qr.dto.response.QrIssueResponse;
import com.daon.daonpay.domain.qr.entity.QrCode;
import com.daon.daonpay.domain.qr.repository.QrCodeRepository;
import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import com.daon.daonpay.global.util.TokenHasher;
import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QrIssueService {

    private final QrCodeRepository qrCodeRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Value("${daonpay.qr.ttl-seconds:60}")
    private long ttlSeconds;

    @PostConstruct
    void validateTtl() {
        if (ttlSeconds <= 0) {
            throw new IllegalStateException("daonpay.qr.ttl-seconds must be positive");
        }
    }

    @Transactional
    public QrIssueResponse issue(Long userId) {
        // 사용자 행 락으로 같은 사용자의 발급을 직렬화
        // (락이 없으면 두 요청이 동시에 "활성 QR 없음"을 보고 둘 다 새 QR을 저장할 수 있다)
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        qrCodeRepository.revokeIssuedByUserId(user.getId());

        Instant now = Instant.now(clock);
        Instant expiresAt = now.plusSeconds(ttlSeconds);
        String token = TokenHasher.randomToken(32);
        qrCodeRepository.save(new QrCode(user.getId(), TokenHasher.sha256Hex(token), expiresAt, now));

        return new QrIssueResponse(token, expiresAt, ttlSeconds);
    }
}