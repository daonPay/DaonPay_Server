package com.daon.daonpay.domain.terminal.service;

import com.daon.daonpay.domain.place.repository.PlaceRepository;
import com.daon.daonpay.domain.terminal.dto.request.TerminalRegisterRequest;
import com.daon.daonpay.domain.terminal.dto.response.TerminalRegisterResponse;
import com.daon.daonpay.domain.terminal.dto.response.TerminalResponse;
import com.daon.daonpay.domain.terminal.entity.Terminal;
import com.daon.daonpay.domain.terminal.entity.TerminalStatus;
import com.daon.daonpay.domain.terminal.repository.TerminalRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import com.daon.daonpay.global.util.DatabaseConstraints;
import com.daon.daonpay.global.util.TokenHasher;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TerminalService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TerminalRepository terminalRepository;
    private final Clock clock;
    private final PlaceRepository placeRepository;
    private final PlatformTransactionManager transactionManager;

    public TerminalRegisterResponse register(TerminalRegisterRequest request) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        for (int attempt = 1; ; attempt++) {
            try {
                return transaction.execute(status -> registerOnce(request));
            } catch (DataIntegrityViolationException e) {
                if (attempt >= 3 || !DatabaseConstraints.matches(e, Terminal.UK_CODE)) {
                    throw e;
                }
            }
        }
    }

    private TerminalRegisterResponse registerOnce(TerminalRegisterRequest request) {
        if (request.placeId() != null && !placeRepository.existsById(request.placeId())) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        String rawSecret = TokenHasher.randomToken(32);
        Terminal terminal = new Terminal(
                generateCode(),
                request.name(),
                request.placeId(),
                TokenHasher.sha256Hex(rawSecret),
                Instant.now(clock));
        terminalRepository.save(terminal);
        return TerminalRegisterResponse.of(terminal, rawSecret);
    }

    @Transactional(readOnly = true)
    public List<TerminalResponse> findAll() {
        return terminalRepository.findAll().stream().map(TerminalResponse::from).toList();
    }

    // 쓰기 락으로 읽고 변경하므로, 진행 중인 QR 검증이 끝난 뒤에 차단이 반영
    @Transactional
    public TerminalResponse changeStatus(Long id, TerminalStatus status) {
        Terminal terminal = terminalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TERMINAL_NOT_FOUND));
        terminal.changeStatus(status);
        return TerminalResponse.from(terminal);
    }

    /**
     * 단말기 인증(+접속 시각 갱신). /authenticate, 검증 결과 재조회처럼
     * 결제 판정과 같은 트랜잭션으로 묶을 필요가 없는 곳에서 쓴다
     */

    @Transactional
    public Terminal authenticate(String terminalCode, String secret) {
        Terminal terminal = checkCredentials(terminalCode, secret);
        terminalRepository.touchLastSeen(terminal.getId(), Instant.now(clock));
        return terminal;
    }

    /**
     * QR 검증 트랜잭션 안에서만 호출한다. 단말기 행에 공유 락을 잡아
     * 인증 → 판정 → 기록이 끝날 때까지 차단/상태 변경이 끼어들지 못하게 한다
     * (여기서 접속 시각은 갱신하지 않는다. 공유 락 보유 중 UPDATE는 교착 위험 존재)
     */

    @Transactional(propagation = Propagation.MANDATORY)
    public Terminal authenticateLocked(String terminalCode, String secret) {
        requireCredentials(terminalCode, secret);
        return check(terminalRepository.findByTerminalCodeForShare(terminalCode.trim()), secret);
    }

    @Transactional
    public void touchLastSeen(Long terminalId) {
        terminalRepository.touchLastSeen(terminalId, Instant.now(clock));
    }

    @Transactional(readOnly = true)
    public Terminal checkCredentials(String terminalCode, String secret) {
        requireCredentials(terminalCode, secret);
        return check(terminalRepository.findByTerminalCode(terminalCode.trim()), secret);
    }

    private void requireCredentials(String terminalCode, String secret) {
        if (terminalCode == null || terminalCode.isBlank() || secret == null || secret.isBlank()) {
            throw new CustomException(ErrorCode.TERMINAL_AUTH_FAILED);
        }
    }

    // 코드 없음/시크릿 불일치는 같은 에러로 응답해 존재 여부를 노출하지 않는다
    private Terminal check(Optional<Terminal> found, String secret) {
        Terminal terminal = found.orElseThrow(() -> new CustomException(ErrorCode.TERMINAL_AUTH_FAILED));
        if (!TokenHasher.matches(secret, terminal.getSecretHash())) {
            throw new CustomException(ErrorCode.TERMINAL_AUTH_FAILED);
        }
        if (!terminal.isActive()) {
            throw new CustomException(ErrorCode.TERMINAL_NOT_ACTIVE);
        }
        return terminal;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder("TERM-");
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }
}