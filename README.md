##  실행 방법

테스트 계정은 환경 변수 설정을 통해 자동 생성할 수 있다.  
비밀번호는 README에 기재하지 않으며, 각자 환경 변수로 설정해야 한다.

### 환경 변수 설정

IntelliJ IDEA의 Run/Debug Configuration에서 Environment variables에 다음 값을 설정한다.

| 환경 변수 | 값 | 설명 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | 로컬 실행 프로필 활성화 |
| `APP_SEED_ACCOUNTS_ENABLED` | `true` | 테스트 계정 자동 생성 활성화 |
| `APP_SEED_ACCOUNTS_PASSWORD` | 각자 설정한 값 | 테스트 계정 공통 비밀번호 (12자 이상) |
| `APP_SEED_ACCOUNTS_ADMIN` | `true` | 관리자 계정 생성 활성화 (선택) |

> 관리자 계정은 `local` 프로필에서 `APP_SEED_ACCOUNTS_ADMIN=true` 설정 시에만 생성된다.

### 생성되는 테스트 계정

| 로그인 ID | 역할 | 비고 |
|---|---|---|
| `test` | `GENERAL` | 회원가입 없이 사용 가능 |
| `general1~3@test.com` | `GENERAL` | 일반 사용자 계정 |
| `vulnerable1~3@test.com` | `VULNERABLE` | 취약 사용자 계정 |
| `admin1~3@test.com` | `ADMIN` | 관리자 계정 (관리자 생성 옵션 활성화 시) |