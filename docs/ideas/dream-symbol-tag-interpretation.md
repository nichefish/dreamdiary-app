# 꿈 태그 상징 해석 (아이디어)

> 상태: **아이디어 (계약 외부)** — 꿈 태그에 별도 해석을 축적하는 확장은 현재 저널·꿈·태그 계약이 아니다. 다만 최소 채택안(A)은 기존 TagProfile 인프라만으로 성립하므로 신규 설계 없이 계약 승격이 가능하다(승격은 새 합의 필요).

## 후보 방향

꿈 태그에 해석을 연결해 반복 상징의 사례와 변주를 누적하는 방안이다. 개별 꿈의 Reflection을 대체하지 않고, 여러 기록에서 반복되는 상징을 별도 기준면으로 다루는 것을 전제로 한다.

## 기존 인프라로 이미 답이 난 부분

`TagProfileEntity`는 `(tag_id, content_type, created_by)` 유니크 제약으로 **사용자별·컨텐츠타입별·태그별** 프로필을 이미 보유한다. 자유서술 본문(`content` LONGTEXT)과 개별 색(`textClass`)을 갖고, 본문은 검색 결과에도 노출된다.

- **소유 단위**: 사용자별 태그 프로필로 확정. `content_type = JOURNAL_DREAM` 필터가 곧 "그 사용자의 꿈 태그"이며, 해석은 기존 `content` 본문에 들어간다.
- **현재 해석 하나**: 현 계약은 태그당 현재 프로필 하나(soft delete). 변경 이력 축적은 미보유 → 확장 후보.
- **검색 소비**: 프로필 본문은 이미 검색 결과에 노출된다.
- **Reflection과의 관계**: Reflection은 엔트리 기준면, 태그 해석은 태그 기준면으로 서로 다른 축이라 같은 슬롯을 다투지 않는다. "충돌 표시"는 두 축을 한 화면에 병치하기로 정한 소비처에서만 생기는 UI 결정이다.

## 최소 채택안 (A)

`JOURNAL_DREAM` TagProfile의 `content`를 "상징 해석" 용도로 자리매김한다. 필요한 것은 신규 데이터 구조가 아니라 UX 명명(라벨·플레이스홀더)과 spec 계약 등재뿐이다. 아래 확장은 포함하지 않는다.

## 남은 열린 질문 (확장 — 각각 독립, 별도 합의 필요)

- **변경 이력 축적**: 태그당 해석 이력을 누적할지. 별도 이력 엔티티가 필요하다(규모 큼).
- **RAG 소비**: 해석 본문을 임베딩·검색증강에 참여시킬지. [reflection-rag-entity-participation.md](reflection-rag-entity-participation.md) 임베딩 설계 보류 건과 함께 판단한다.
- **연간 결산 소비**: 결산 태그행에 해석을 노출할지.

## 관련 현재 계약

- [DESIGN_NOTES.md](../spec/DESIGN_NOTES.md)
- [JOURNAL_SCREEN_BEHAVIOR_SPEC.md](../spec/JOURNAL_SCREEN_BEHAVIOR_SPEC.md)