package com.daon.daonpay.domain.qr;

import com.daon.daonpay.domain.qr.dto.request.QrVerifyRequest;
import com.daon.daonpay.domain.qr.dto.response.QrIssueResponse;
import com.daon.daonpay.domain.qr.dto.response.QrVerifyResponse;
import com.daon.daonpay.domain.qr.service.QrIssueService;
import com.daon.daonpay.domain.qr.service.QrVerifyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * QR 발급 및 검증 요청을 처리하는 컨트롤러.

 * 사용자 앱에서는 결제용 QR을 발급하고,
 * 가맹점 단말기에서는 QR 검증 및 검증 결과 재조회를 수행한다.
 */

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {

    // 단말기 인증에 사용하는 단말기 코드 헤더 이름
    private static final String TERMINAL_CODE = "X-Terminal-Code";

    // 단말기 인증에 사용하는 비밀 키 헤더 이름
    private static final String TERMINAL_SECRET = "X-Terminal-Secret";

    // 결제용 QR 발급을 담당하는 서비스
    private final QrIssueService qrIssueService;

    // QR 검증 및 검증 결과 조회를 담당하는 서비스
    private final QrVerifyService qrVerifyService;

    /**
     * 로그인한 사용자에게 결제용 QR 코드를 발급

     * 인증 객체에서 로그인한 사용자의 식별 정보를 가져와
     * 해당 사용자에게 연결된 QR 발급을 서비스에 요청

     * @param authentication 현재 로그인한 사용자의 인증 정보
     * @return 발급된 QR 정보
     */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QrIssueResponse issue(Authentication authentication) {
        return qrIssueService.issue(authentication.getName());
    }

    /**
     * 가맹점 단말기에서 전달한 QR 코드의 유효성을 검증

     * 단말기 코드와 비밀 키를 헤더로 전달받고,
     * 요청 본문에 포함된 QR 검증 정보를 서비스에 전달

     * @param terminalCode 단말기 식별 코드
     * @param terminalSecret 단말기 인증 비밀 키
     * @param request QR 검증 요청 데이터
     * @return QR 검증 결과 및 결제 가능 여부
     */

    @PostMapping("/verify")
    public QrVerifyResponse verify(
            @RequestHeader(value = TERMINAL_CODE, required = false) String terminalCode,
            @RequestHeader(value = TERMINAL_SECRET, required = false) String terminalSecret,
            @Valid @RequestBody QrVerifyRequest request) {

        // 전달받은 단말기 인증 정보와 QR 데이터를 서비스에 전달
        // 검증 결과에 따라 결제 가능 여부를 반환
        return qrVerifyService.verify(terminalCode, terminalSecret, request);
    }

    /**
     * 단말기의 응답이 끊겼을 때 requestId를 이용해
     * 이전 QR 검증 결과를 다시 조회한다.
     *
     * 동일한 요청의 처리 결과를 확인할 수 있도록
     * 단말기 인증 정보와 요청 ID를 서비스에 전달한다.
     *
     * @param terminalCode 단말기 식별 코드
     * @param terminalSecret 단말기 인증 비밀 키
     * @param requestId 이전 QR 검증 요청의 식별자
     * @return 기존 QR 검증 결과
     */

    @GetMapping("/verifications/{requestId}")
    public QrVerifyResponse findVerification(
            @RequestHeader(value = TERMINAL_CODE, required = false) String terminalCode,
            @RequestHeader(value = TERMINAL_SECRET, required = false) String terminalSecret,
            @PathVariable String requestId) {

        // requestId를 기준으로 기존 검증 결과 조회
        return qrVerifyService.findByRequestId(terminalCode, terminalSecret, requestId);
    }
}
