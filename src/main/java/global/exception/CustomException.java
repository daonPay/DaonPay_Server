package global.exception;

import lombok.Getter;

// 사용자 정의 예외 클래스
@Getter
public class CustomException extends RuntimeException {

    // 발생한 예외의 상태 코드, 에러 코드, 메시지 정보
    private final ErrorCode errorCode;

    // ErrorCode를 전달받아 사용자 정의 예외 생성
    public CustomException(ErrorCode errorCode) {
        // RuntimeException에 예외 메시지 설정
        super(errorCode.getMessage());

        // 전달받은 에러 코드 저장
        this.errorCode = errorCode;
    }
}

