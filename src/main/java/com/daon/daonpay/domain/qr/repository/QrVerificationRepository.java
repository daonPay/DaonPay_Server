package com.daon.daonpay.domain.qr.repository;

import com.daon.daonpay.domain.qr.entity.QrVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrVerificationRepository extends JpaRepository<QrVerification, Long> {

    Optional<QrVerification> findByTerminalIdAndRequestId(Long terminalId, String requestId);
}