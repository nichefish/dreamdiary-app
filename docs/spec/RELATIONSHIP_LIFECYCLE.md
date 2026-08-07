# 관계 생명주기 의미론 (Relationship Lifecycle Semantics)

**상태: 보류(Deferred / TODO).** 이 문서는 도메인 관계마다 삭제·복원·이동·복제·병합의 의미를 **관계 타입 단위**로 정의하는 상위 설계다. Reflection 되가르기 마이그레이션을 막지 않기 위해 **본격 작성은 미룬다** — 지금은 원리 스케치와 잠정 규칙만 남긴다.

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

## 스코프 (본격 작성 시)

- **delete 우선.** restore/move/copy/merge 는 이후.
- 기존 관계(chapter·tag·comment·embedding·person)는 **현행 동작을 코드로 확인해 as-is 로 기술** — 재결정 아님.
- 신규 결정은 관계 타입별 원리 확정 + 기존과의 정합성 검토.

## 잠정 적용 (이 문서 확정 전)

- **entry → reflection = Reference → Block(재귀) + 명시 승인 cascade(서브트리)**. Reflection 되가르기 마이그레이션 R3 가 이 잠정 규칙을 적용한다. ([reflection-domain-model.md](../migration/journal/reflection-domain-model.md) §5)