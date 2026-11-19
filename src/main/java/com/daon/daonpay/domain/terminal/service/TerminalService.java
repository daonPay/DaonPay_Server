package com.daon.daonpay.domain.terminal.service;

import com.daon.daonpay.domain.terminal.dto.request.TerminalRegisterRequest;
import com.daon.daonpay.domain.terminal.dto.response.TerminalRegisterResponse;
import com.daon.daonpay.domain.terminal.dto.response.TerminalResponse;
import com.daon.daonpay.domain.terminal.entity.Terminal;
import com.daon.daonpay.domain.terminal.entity.TerminalStatus;
import com.daon.daonpay.domain.terminal.repository.TerminalRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import com.daon.daonpay.global.util.TokenHasher;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TerminalService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TerminalRepository terminalRepository;

    @Transactional
    public TerminalRegisterResponse register(TerminalRegisterRequest request) {
        String rawSecret = TokenHasher.randomToken(32);
        Terminal terminal = new Terminal(
                generateCode(),
                request.name(),
                request.placeId(),
                TokenHasher.sha256Hex(rawSecret));
        terminalRepository.save(terminal);
        return TerminalRegisterResponse.of(terminal, rawSecret);
    }

    @Transactional(readOnly = true)
    public List<TerminalResponse> findAll() {
        return terminalRepository.findAll().stream().map(TerminalResponse::from).toList();
    }

    @Transactional
    public TerminalResponse changeStatus(Long id, TerminalStatus status) {
        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TERMINAL_NOT_FOUND));
        terminal.changeStatus(status);
        return TerminalResponse.from(terminal);
    }

    /**
     * 단말기 코드/시크릿 검증. 성공 시 마지막 접속 시각을 갱신하고 단말기를 반환
     * 코드가 없거나 시크릿이 다르면 동일하게 TERMINAL_AUTH_FAILED로 응답해 존재 여부를 노출하지 않음
     */

    @Transactional
    public Terminal authenticate(String terminalCode, String secret) {
        if (terminalCode == null || terminalCode.isBlank() || secret == null || secret.isBlank()) {
            throw new CustomException(ErrorCode.TERMINAL_AUTH_FAILED);
        }
        Terminal terminal = terminalRepository.findByTerminalCode(terminalCode.trim())
                .orElseThrow(() -> new CustomException(ErrorCode.TERMINAL_AUTH_FAILED));
        if (!TokenHasher.matches(secret, terminal.getSecretHash())) {
            throw new CustomException(ErrorCode.TERMINAL_AUTH_FAILED);
        }
        if (!terminal.isActive()) {
            throw new CustomException(ErrorCode.TERMINAL_NOT_ACTIVE);
        }
        terminal.markSeen();
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