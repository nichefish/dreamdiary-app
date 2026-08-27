# Reflection과 조직 aggregate 경계 (아이디어)

> 상태: **아이디어 (계약 외부)** — 현재 Reflection 영속·표시·삭제 계약을 변경하지 않는다.

## 열린 질문

챕터의 정렬·소속과 스레드의 엔트리 소속을 Reflection과 독립된 조직(organizational) aggregate라는 하나의 도메인 개념으로 일반화할 필요가 있는지 검토한다.

- 일반화가 실제 API·영속·화면 소비처를 단순화하는가
- 챕터의 일자 내부 containment와 스레드의 N:M 소속 차이를 같은 추상화가 보존할 수 있는가
- 현재 단일 경로 위에 불필요한 공통 계층을 추가하지 않는가

## 관련 현재 계약

- [reflection-domain-model.md](../migration/journal/reflection-domain-model.md)
- [REFLECTION_ONE_TYPE.md](../spec/REFLECTION_ONE_TYPE.md)
