# JournalDay 사용자 소유권

## 현재 계약

`journal_day`는 저널 본문 트리의 소유권 루트다. 소유권과 감사 기록은 다음 컬럼으로 분리한다.

| 컬럼 | 계약 |
|---|---|
| `owner_id` | 현재 소유 사용자의 `user.id`. 조회·인가·사용자별 일자 중복 검사의 기준이다. |
| `created_by` | 생성 당시 행위자의 username 감사 스냅샷이다. |
| `updated_by` | 마지막 수정 행위자의 username 감사 스냅샷이다. |

`owner_id`는 등록 요청에서 받지 않는다. 서버가 인증 객체의 `userId`로 설정하며 일반 수정 경로에서는 변경하지 않는다. `created_by`와 `updated_by`는 기존 JPA Auditing 경로가 계속 기록한다.

현재 소유권 컬럼 적용 대상은 `journal_day`다. 챕터 생성과 일자 부트스트랩처럼 Day를 직접 여는 경로는 `journal_day.owner_id`를 검사한다.

## 운영 DB 적용 SQL

애플리케이션은 `journal_day.owner_id`를 항상 조회하므로 다음 SQL을 애플리케이션 기동 전에 적용한다. 백필은 활성 사용자 중 username이 정확히 한 행인 경우만 수행한다. 미매핑 또는 중복 사용자가 있으면 검증 조회에 남고 `NOT NULL` 전환이 실패하여 비정상 소유권을 감추지 않는다.

```sql
ALTER TABLE journal_day
    ADD COLUMN owner_id INT NULL COMMENT '저널 일자 소유 사용자 ID' AFTER content_type;

UPDATE journal_day day
INNER JOIN (
    SELECT username, MIN(id) AS owner_id
    FROM user
    WHERE deleted_at IS NULL
    GROUP BY username
    HAVING COUNT(*) = 1
) matched_user
    ON matched_user.username = day.created_by
SET day.owner_id = matched_user.owner_id
WHERE day.owner_id IS NULL;

SELECT COUNT(*) AS missing_owner_count
FROM journal_day
WHERE owner_id IS NULL;

ALTER TABLE journal_day
    MODIFY COLUMN owner_id INT NOT NULL COMMENT '저널 일자 소유 사용자 ID',
    ADD INDEX idx_journal_day_owner (owner_id),
    ADD CONSTRAINT fk_journal_day_owner
        FOREIGN KEY (owner_id) REFERENCES user(id);
```

적용 후 다음 검증 결과는 모두 `0`이어야 한다.

```sql
SELECT COUNT(*) AS missing_owner_count
FROM journal_day
WHERE owner_id IS NULL;

SELECT COUNT(*) AS invalid_owner_count
FROM journal_day day
LEFT JOIN user owner ON owner.id = day.owner_id
WHERE owner.id IS NULL;
```
