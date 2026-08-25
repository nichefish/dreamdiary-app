# Prefix 기능 확장 (아이디어)

> 상태: **아이디어 (계약 외부)** — 게시판 간 복사와 관리 대상 도메인 확장은 현재 Prefix 계약이 아니다.

## 게시판 간 일회성 복사 후보

현재 게시판은 `GLOBAL + boardKey` Scope별로 독립된 Prefix 목록을 가진다. 실제 재사용 요구가 확인되면 Scope를 실시간 공유하지 않고 다른 게시판의 Prefix를 한 번 복제하는 기능을 둘 수 있다.

열린 계약은 복사 대상 필드, 이름 충돌 처리, 비활성 Prefix 포함 여부, 복사 후 두 목록의 완전한 독립성이다.

## 개인 관리 카탈로그 확장 후보

현재 `/my/prefixes`는 저널의 `일기 챕터 / 노트 챕터 / 일기 / 꿈 / 노트 / 스레드` 6개 대상만 표시한다. 다른 개인 도메인이 Prefix를 실제 소비하게 되면 같은 화면에서 별도 도메인 그룹으로 관리 대상을 확장하는 방안을 검토한다.

## 관련 현재 계약

- [PREFIX_SCOPE_DESIGN.md](../spec/PREFIX_SCOPE_DESIGN.md)
- [인증/사용자 화면 spec](../migration/auth/screen-spec.md)
