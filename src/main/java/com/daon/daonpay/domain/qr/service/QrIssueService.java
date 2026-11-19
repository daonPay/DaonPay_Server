package com.daon.daonpay.domain.qr.service;

import com.daon.daonpay.domain.qr.dto.response.QrIssueResponse;
import com.daon.daonpay.domain.qr.entity.QrCode;
import com.daon.daonpay.domain.qr.repository.QrCodeRepository;
import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import com.daon.daonpay.global.util.TokenHasher;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QrIssueService {

    private final QrCodeRepository qrCodeRepository;
    private final UserRepository userRepository;

    @Value("${daonpay.qr.ttl-seconds:60}")
    private long ttlSeconds;

    @Transactional
    public QrIssueResponse issue(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        qrCodeRepository.revokeIssuedByUserId(user.getId());

        String token = TokenHasher.randomToken(32);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(ttlSeconds);
        qrCodeRepository.save(new QrCode(user.getId(), TokenHasher.sha256Hex(token), expiresAt));

        return new QrIssueResponse(token, expiresAt, ttlSeconds);
    }
}