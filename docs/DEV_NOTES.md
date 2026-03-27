# DEV NOTES

## 패키지 구조
- "패키지는 파일 묶음이 아니라 책임 경계다."
- `feature`는 사용자 시나리오와 비즈니스 규칙에만 집중한다.
- "`global` 상수는 `auth`/`feature`/`infrastructure`를 import하면 안 된다."
- "`feature`-`infrastructure` 계층간 협력은 구현체가 아니라 명시적 계약(포트/서비스)으로 연결한다."
