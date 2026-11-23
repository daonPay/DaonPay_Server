package com.daon.daonpay.domain.qr.service;

import com.daon.daonpay.domain.place.dto.request.PlaceStatusUpdateRequest;
import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import com.daon.daonpay.domain.place.entity.PlaceStatus;
import com.daon.daonpay.domain.place.repository.PlaceRepository;
import com.daon.daonpay.domain.place.service.PlaceService;
import com.daon.daonpay.domain.qr.dto.request.QrVerifyRequest;
import com.daon.daonpay.domain.qr.entity.VerifyResult;
import com.daon.daonpay.domain.terminal.dto.request.TerminalRegisterRequest;
import com.daon.daonpay.domain.terminal.repository.TerminalRepository;
import com.daon.daonpay.domain.terminal.service.TerminalService;
import com.daon.daonpay.domain.user.entity.Role;
import com.daon.daonpay.domain.user.entity.User;
import com.daon.daonpay.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DAONPAY_QR_TEST_DB", matches = "true")
class QrMySqlIntegrationTest {
    @Autowired private QrIssueService issueService;
    @Autowired private QrVerifyService verifyService;
    @Autowired private TerminalService terminalService;
    @Autowired private TerminalRepository terminalRepository;
    @Autowired private PlaceRepository placeRepository;
    @Autowired private PlaceService placeService;
    @Autowired private UserRepository userRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    void onlyOneOfTwoConcurrentVerificationsConsumesTheQr() throws Exception {
        var place = placeRepository.save(new Place("QR test", PlaceCategory.MERCHANT, "서울", "중구", null, null));
        var terminal = terminalService.register(new TerminalRegisterRequest("QR test", place.getId()));
        var user = userRepository.save(User.builder().email(UUID.randomUUID() + "@example.test")
                .name("QR test").password("unused").role(Role.GENERAL).build());
        var qr = issueService.issue(user.getId());
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> {
                start.await();
                return verifyService.verify(terminal.terminalCode(), terminal.secret(),
                        new QrVerifyRequest("first", qr.qrToken())).result();
            });
            var second = pool.submit(() -> {
                start.await();
                return verifyService.verify(terminal.terminalCode(), terminal.secret(),
                        new QrVerifyRequest("second", qr.qrToken())).result();
            });
            start.countDown();
            assertThat(java.util.List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(VerifyResult.APPROVED, VerifyResult.QR_ALREADY_USED);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void closedPlaceDoesNotConsumeQrAndOlderTouchDoesNotReplaceNewerTime() {
        var place = placeRepository.save(new Place("QR test", PlaceCategory.MERCHANT, "서울", "중구", null, null));
        var terminal = terminalService.register(new TerminalRegisterRequest("QR test", place.getId()));
        var user = userRepository.save(User.builder().email(UUID.randomUUID() + "@example.test")
                .name("QR test").password("unused").role(Role.GENERAL).build());
        var qr = issueService.issue(user.getId());
        placeService.updateStatus(place.getId(), new PlaceStatusUpdateRequest(PlaceStatus.CLOSED));
        assertThat(verifyService.verify(terminal.terminalCode(), terminal.secret(),
                new QrVerifyRequest("closed", qr.qrToken())).result()).isEqualTo(VerifyResult.PLACE_UNAVAILABLE);
        placeService.updateStatus(place.getId(), new PlaceStatusUpdateRequest(PlaceStatus.OPEN));
        assertThat(verifyService.verify(terminal.terminalCode(), terminal.secret(),
                new QrVerifyRequest("open", qr.qrToken())).result()).isEqualTo(VerifyResult.APPROVED);

        var latest = Instant.now().plusSeconds(60).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            assertThat(terminalRepository.touchLastSeen(terminal.id(), latest)).isEqualTo(1);
            assertThat(terminalRepository.touchLastSeen(terminal.id(), latest.minusSeconds(1))).isZero();
        });
        assertThat(terminalRepository.findById(terminal.id()).orElseThrow().getLastSeenAt()).isEqualTo(latest);
    }
}
