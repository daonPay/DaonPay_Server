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
import com.daon.daonpay.global.util.TokenHasher;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QrVerificationTimingTest {
    @Test
    void qrThatExpiresDuringLockAcquisitionIsNotConsumed() {
        var terminals = mock(TerminalService.class);
        var codes = mock(QrCodeRepository.class);
        var verifications = mock(QrVerificationRepository.class);
        var places = mock(PlaceAvailabilityChecker.class);
        var clock = mock(Clock.class);
        var processor = new QrVerifyProcessor(terminals, codes, verifications, places, clock);
        var expires = Instant.parse("2026-11-23T00:00:01Z");
        String hash = TokenHasher.sha256Hex("token");
        var qr = new QrCode(1L, hash, expires, expires.minusSeconds(1));
        var terminal = mock(Terminal.class);

        when(terminals.authenticateLocked("terminal", "secret")).thenReturn(terminal);
        when(terminal.getId()).thenReturn(2L);
        when(terminal.getPlaceId()).thenReturn(3L);
        when(places.isAvailable(3L)).thenReturn(true);
        when(clock.instant()).thenReturn(expires.minusSeconds(1));
        when(codes.findByTokenHashForUpdate(hash)).thenAnswer(invocation -> {
            when(clock.instant()).thenReturn(expires);
            return Optional.of(qr);
        });
        when(verifications.saveAndFlush(any(QrVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var verification = processor.process("terminal", "secret", new QrVerifyRequest("request", "token"));
        assertThat(verification.getResult()).isEqualTo(VerifyResult.QR_EXPIRED);
        assertThat(qr.getStatus()).isEqualTo(QrStatus.ISSUED);
    }

    @Test
    void lastSeenFailureDoesNotHideCommittedVerification() {
        var terminals = mock(TerminalService.class);
        var processor = mock(QrVerifyProcessor.class);
        var verifications = mock(QrVerificationRepository.class);
        var service = new QrVerifyService(terminals, processor, verifications);
        var request = new QrVerifyRequest("request", "token");
        when(processor.process("terminal", "secret", request)).thenReturn(new QrVerification(
                2L, "request", TokenHasher.sha256Hex("token"), 1L, VerifyResult.APPROVED, Instant.EPOCH));
        doThrow(new DataAccessResourceFailureException("test failure")).when(terminals).touchLastSeen(2L);

        assertThat(service.verify("terminal", "secret", request).result()).isEqualTo(VerifyResult.APPROVED);
    }

    @Test
    void lastSeenFailureDoesNotHideRetrievedVerification() {
        var terminals = mock(TerminalService.class);
        var processor = mock(QrVerifyProcessor.class);
        var verifications = mock(QrVerificationRepository.class);
        var service = new QrVerifyService(terminals, processor, verifications);
        var terminal = mock(Terminal.class);
        when(terminals.checkCredentials("terminal", "secret")).thenReturn(terminal);
        when(terminal.getId()).thenReturn(2L);
        when(verifications.findByTerminalIdAndRequestId(2L, "request")).thenReturn(Optional.of(
                new QrVerification(2L, "request", TokenHasher.sha256Hex("token"), 1L,
                        VerifyResult.APPROVED, Instant.EPOCH)));
        doThrow(new DataAccessResourceFailureException("test failure")).when(terminals).touchLastSeen(2L);

        assertThat(service.findByRequestId("terminal", "secret", "REQUEST").result())
                .isEqualTo(VerifyResult.APPROVED);
    }
}
