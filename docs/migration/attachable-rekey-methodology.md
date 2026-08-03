# Attachable 재키잉 방법론 — 엔티티 이동·un-merge 시 사이드테이블 계약

DreamDiary 의 콘텐츠 엔티티(일기·꿈·노트·Reflection 등)는 자신에게 딸린 부가 정보를
**attachable 사이드테이블**에 `(ref_id, ref_content_type)` 키로 보관한다. 엔티티를 다른 테이블로
옮기거나(STI un-merge, 테이블 분리) id 를 재발급하는 마이그레이션은 **본문 행만 옮기면 끝나지
않는다.** 사이드테이블 행이 옛 키를 가리킨 채 남으면 **고아(orphan)** 가 되고, 새 엔티티가 옛
id 범위와 겹치면 **엉뚱한 엔티티에 남의 부가정보가 조인되는 조용한 오염**이 발생한다.

이 문서는 그 재키잉 계약과 검증 절차를 규정한다.

## 1. 불변식 (Invariant)

> 콘텐츠 엔티티 A 의 식별(테이블·id)이 바뀌는 마이그레이션은, A 를 `ref_id` 로 가리키던 **모든
> attachable 사이드테이블 행**을 같은 트랜잭션 안에서 새 식별로 이관(재키잉)하거나, **id 를 보존**해야
> 한다. 둘 중 하나를 하지 않으면 마이그레이션은 미완이다.

"본문만 옮기고 커밋" 은 미완이며, 릴리즈 전이라도 조용한 데이터 오염을 남긴다.

## 2. attachable 사이드테이블 목록 — `(ref_id, ref_content_type)` 키

엔티티 이동 시 아래 테이블 전부를 대상 엔티티의 `content_type` 으로 조회해 처리 여부를 판정한다.

| 테이블 | 내용 | 비고 |
|---|---|---|
| `state` | 중요/참조/접힘 등 상태 플래그 | 표시 뱃지·상태 캐시 소스 |
| `lifecycle` | OPEN/RESOLVED 등 생명주기 | 완료 표시·연쇄 소스 |
| `comment` | 댓글 | |
| `history` | 변경 이력 로그 | 감사용 |
| `tag_content` | 태그 연결 | 태그 요약·검색 축 |
| `meta_content` | 메타 연결 | |
| `viewer` | 열람자 | |
| `managtr` | 관리자 지정 | |
| `prefix_content` | 말머리 선택 연결 | |

**owner 컬럼 관계는 재키잉 대상이 아니다.** 본문 행 자신이 보유하는 컬럼은 행과 함께 이동하므로
보존된다:
- `file` : 본문 행의 `file_group_id` (첨부파일 그룹)
- `history_triggered_by` / `history_triggered_at` : 본문 행의 최종 트리거 스냅샷

즉 첨부파일은 이동해도 살아남지만, **history 로그 테이블**은 `(ref_id, ref_content_type)` 키라 별도
재키잉이 필요하다(스냅샷 컬럼과 혼동 금지).

## 3. 두 가지 유효 전략

### (a) id 보존 (권장)
새 테이블에 **옛 id 를 그대로** `INSERT ... SELECT id, ...` 로 옮긴다. 사이드테이블은 `ref_id` 를
건드릴 필요가 없고(같은 id), `ref_content_type` 만 새 타입으로 바뀌면 되는 경우 그것만 갱신한다.
연결이 끊기지 않아 가장 안전하다.

### (b) id 재발급 + 사이드테이블 재키잉
새 테이블이 자체 시퀀스로 새 id 를 발급한다면, **옛 id → 새 id 매핑 테이블을 먼저 만들고**
모든 사이드테이블의 `(ref_id, ref_content_type)` 를 그 매핑으로 갱신한다. 매핑은 본문 이관과
**같은 트랜잭션**에서 확보해야 한다 — 본문 이관 후 옛 행을 삭제하면 매핑 근거가 사라진다.

## 4. 금지 — 매핑 근거 소실

다음을 **하지 마라**:
- 새 테이블에 새 id 로 본문만 넣고, 옛 본문 행을 **하드 삭제**해 옛 id↔본문 연결을 없앤다.
- 새 본문 행에 옛 id 를 보존하는 컬럼을 두지 않는다.

둘 다 하면 옛 사이드테이블 행(옛 id 를 가리킴)과 새 본문 행(새 id) 사이에 **공통 키가 양쪽 모두
소실**되어, 데이터만으로는 재키잉이 **복구 불가능**해진다. 사이드테이블은 통째로 손실 처리하는 수밖에
없다.

## 5. 검증 절차 (마이그레이션 SAVEPOINT 필수)

이관 후 대상 `content_type` 에 대해 아래를 확인한다.

```sql
-- 새 본문 id 범위
SELECT MIN(id) lo, MAX(id) hi, COUNT(*) n FROM <새_테이블>;

-- 각 사이드테이블: 새 범위 밖(고아) / 새 범위 안(충돌 위험) 집계
SELECT '<사이드테이블>' t, COUNT(*) n,
       SUM(ref_id NOT IN (SELECT id FROM <새_테이블>)) AS orphan_or_collision
FROM <사이드테이블> WHERE ref_content_type='<CONTENT_TYPE>';
```

- 새 범위 **밖** ref_id: 고아. 조인 안 되지만 쓰레기 → 정리.
- 새 범위 **안**인데 옛 엔티티에서 온 ref_id: **충돌**. 새 엔티티에 남의 부가정보가 붙는다 → 즉시 조치.

`ref_id` 가 새 본문 id 집합과 **완전히 일치**해야 이관 성공이다.

## 6. 워크드 예시 — Reflection un-merge (R2) 의 미스

`journal_reflection` 분리(R2)는 본문 55건을 **새 순번 id(1~55)** 로 옮기고 옛 `journal_entry`
REFLECTION 행을 **하드 삭제**했으나, 사이드테이블을 재키잉하지 않았다. 결과:

| 테이블 | 고아 행 | 새 범위(1~55) 충돌 | 결과 |
|---|---|---|---|
| `state` | 35 | 34 | 34개 reflection 에 남의 상태 오염 |
| `lifecycle` | 37 | 다수 | 완료 표시 오염 |
| `history` | 17 (13273~13304) | 0 | 감사로그 고아 |
| `tag_content` | 2 (13265) | 0 | reflection 태그 2건 소실 |

옛 `journal_entry` REFLECTION 행이 하드 삭제되고 `journal_reflection` 에 옛 id 보존 컬럼도 없어,
**옛 사이드테이블 ↔ 새 reflection 매핑을 복구할 수 없다**(§4 금지 위반). 따라서 사이드테이블은
손실 수용 후 고아 정리로 수렴한다.

```sql
-- Reflection 고아 사이드테이블 정리 (매핑 복구 불가 → 손실 수용)
DELETE FROM state          WHERE ref_content_type='JOURNAL_REFLECTION';
DELETE FROM lifecycle      WHERE ref_content_type='JOURNAL_REFLECTION';
DELETE FROM history        WHERE ref_content_type='JOURNAL_REFLECTION';
DELETE FROM tag_content    WHERE ref_content_type='JOURNAL_REFLECTION';
```

정리 후 state/lifecycle 은 `journal_reflection.id` 기준으로 새로 쌓이고(R3b 읽기 경로가 이 축을
읽는다), 향후 토글·태그는 새 id 로 정상 기록된다.

**교훈:** un-merge SQL 을 작성할 때 §2 의 사이드테이블 체크리스트를 SQL 안에 명시적으로 포함하고,
§5 검증을 SAVEPOINT 게이트로 돌려라. 본문 이관만으로 "완료" 를 선언하지 마라.
