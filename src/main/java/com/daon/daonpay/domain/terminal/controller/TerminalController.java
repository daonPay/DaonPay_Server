package com.daon.daonpay.domain.terminal.controller;

import com.daon.daonpay.domain.terminal.dto.request.TerminalAuthRequest;
import com.daon.daonpay.domain.terminal.dto.request.TerminalRegisterRequest;
import com.daon.daonpay.domain.terminal.dto.request.TerminalStatusRequest;
import com.daon.daonpay.domain.terminal.dto.response.TerminalAuthResponse;
import com.daon.daonpay.domain.terminal.dto.response.TerminalRegisterResponse;
import com.daon.daonpay.domain.terminal.dto.response.TerminalResponse;
import com.daon.daonpay.domain.terminal.service.TerminalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/terminals")
@RequiredArgsConstructor
public class TerminalController {

    private final TerminalService terminalService;

    // 관리자: 단말기 등록 (시크릿은 이 응답에서만 확인 가능)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TerminalRegisterResponse register(@Valid @RequestBody TerminalRegisterRequest request) {
        return terminalService.register(request);
    }

    // 관리자: 단말기 목록
    @GetMapping
    public List<TerminalResponse> findAll() {
        return terminalService.findAll();
    }

    // 관리자: 단말기 상태 변경(ACTIVE/INACTIVE/BLOCKED)
    @PatchMapping("/{id}/status")
    public TerminalResponse changeStatus(@PathVariable Long id, @Valid @RequestBody TerminalStatusRequest request) {
        return terminalService.changeStatus(id, request.status());
    }

    // 단말기: 부팅/헬스체크 시 인증 (공개 경로, 코드+시크릿으로 검증)
    @PostMapping("/authenticate")
    public TerminalAuthResponse authenticate(@Valid @RequestBody TerminalAuthRequest request) {
        return TerminalAuthResponse.from(terminalService.authenticate(request.terminalCode(), request.secret()));
    }
}