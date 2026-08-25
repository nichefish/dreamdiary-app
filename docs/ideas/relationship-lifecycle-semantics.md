# 관계 생명주기 의미론 (아이디어)

> 상태: **아이디어 (계약 외부)** — 관계별 생명주기 규칙을 하나의 상위 의미론으로 일반화할지는 합의되지 않았다. 이 문서는 원리 스케치이며 현재 시스템 계약이 아니다.

> 계기: Reflection 삭제 정책을 정하다, 이것이 Reflection 고유 문제가 아니라 **관계 타입(About/Reference)** 의 문제임이 드러났다. 같은 질문이 comment·tag·chapter·person·embedding 에도 동일하게 적용된다(같은 문제의 다른 얼굴).

---

## 원리 (스케치)

삭제 정책은 **엔티티가 아니라 관계 타입**에 붙는다:

| 관계 타입 | 예 | 삭제 |
|---|---|---|
| Ownership / Containment | chapter → entry | **Cascade** |
| Reference / Commentary | entry → reflection (comment?) | **Block + 명시 cascade** |
| Classification | entry → tag | **Unlink** |
| Derivation | entry → embedding·entity | **Rebuild** |
| Attribution | person → entry | **Preserve**(작성자) |

**교차 규칙**: Ownership/Containment cascade 가 Reference 를 **소리 없이 orphan 화하면 안 된다**. (예: chapter 삭제→entry cascade 가 그 entry 를 가리키는 Reflection 을 orphan 으로 만들면 안 됨 → 동일 block.)

## 검토 범위

- 첫 검토 범위는 delete이며 restore/move/copy/merge는 열린 주제다.
- 기존 관계(chapter·tag·comment·embedding·person)는 **현행 동작을 코드로 확인해 as-is 로 기술** — 재결정 아님.
- 신규 결정은 관계 타입별 원리 확정 + 기존과의 정합성 검토.

## 현재 계약과의 경계

- Reflection 대상 삭제의 현재 동작은 [reflection-domain-model.md](../migration/journal/reflection-domain-model.md) §5가 정본이다. 그 동작이 존재한다는 사실만으로 이 문서의 전체 관계 분류가 시스템 계약이 되지는 않는다.

## Reflection 삭제 경험 후보

- 사용자가 대상과 참조 Reflection 서브트리를 명시적으로 확인한 뒤 함께 삭제하는 cascade 동작을 제공할지는 열린 질문이다.
- 확인 UI, 삭제 원자성, 하위 참조 탐색 범위가 합의되어야 계약으로 전환할 수 있다.
