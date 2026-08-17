# 저널 사용자별 기본 진입점

## 목적

저널 일자 영역의 제품 화면 URL을 `/app/journal/day/home`으로 모으고, 현재 프론트엔드의 `/journal/day/home` route에서 로그인 사용자가 선택한 일간·주간·월간 화면으로 이동한다. 사용자가 직접 연 명시적 저널 경로와 query는 보존한다.

## 확정 계약

- 설정 키는 URL이 아니라 `DAILY | WEEKLY | MONTHLY` enum으로 저장한다.
- 설정이 없을 때 기본값은 `DAILY`다.
- `DAILY`는 오늘, `WEEKLY`는 현재 주, `MONTHLY`는 현재 월을 기준으로 연다.
- 메뉴 DB와 외부 링크는 프론트엔드 구현체와 독립적인 제품 화면 URL `/app/journal/day/home`을 사용한다. 이 URL은 현재 활성 화면 구현체인 `/vue-app/journal/day/home`으로 연결된다.
- Vue 앱 내부의 `/`, `/journal`, 로그인 기본 이동, 사이드바 로고, 사용자 모드 복귀는 `/journal/day/home` route를 공통 진입점으로 사용한다.
- `/journal/daily`, `/journal/weekly`, `/journal/monthly`와 해당 query를 직접 연 경우에는 사용자 설정을 적용하지 않는다.
- 마지막 방문 화면은 저장하지 않는다.
- 설정 API 실패를 `DAILY` 이동으로 가장하지 않고 앱 런타임 오류로 표시한다.

## 데이터·API 계약

`journal_setting`은 `(scope, scope_key)` 조합으로 설정 소유 범위를 구분한다.

| 범위 | 키 | 담당 설정 |
|---|---|---|
| `ADMIN` | `GLOBAL` | `embedding_enabled` 전역 정책 |
| `USER` | 로그인 username | `default_entry_view` 사용자 정책 |

- `GET /api/journal/settings/me`: 사용자 행이 없거나 값이 비어 있으면 DB 쓰기 없이 `DAILY`를 반환한다.
- `PUT /api/journal/settings/me`: 인증 정보의 username으로 사용자 행을 최초 생성하거나 갱신한다.
- 요청 본문은 `defaultEntryView`만 받으며 username을 받지 않는다.
- `(scope, scope_key)`는 UNIQUE 제약으로 중복 행을 차단한다.

## 구현 현황

| 영역 | 상태 | 현재 계약 |
|---|---|---|
| 선언 스키마·Entity | ✓ | `default_entry_view`, `(scope, scope_key)` UNIQUE |
| 사용자 설정 API | ✓ | `GET/PUT /api/journal/settings/me` |
| 내 설정 UI | ✓ | `/my/journal`에서 일간·주간·월간을 선택하고 저장 |
| 공통 진입 resolver | ✓ | Vue `/journal/day/home` route에서 사용자 설정을 명시적 route로 해석 |
| 메뉴·공통 진입 경로 | ✓ | `JOURNAL_DAY` 메뉴는 제품 화면 URL `/app/journal/day/home` 사용 |

## 운영 DB 반영

1.0 이전 선언 스키마의 단일 진실 원천은 `app/backend/src/main/resources/schema/full/mariadb/schema-journal-mariadb.sql`이다. 기존 운영 DB에는 적용 전 중복 여부를 확인하고 같은 계약으로 컬럼과 UNIQUE 제약을 반영한다.

```sql
SELECT scope, scope_key, COUNT(*) AS row_count
FROM journal_setting
GROUP BY scope, scope_key
HAVING COUNT(*) > 1;

ALTER TABLE journal_setting
    ADD COLUMN default_entry_view VARCHAR(20) NULL
        COMMENT '사용자별 저널 기본 진입 화면 (DAILY/WEEKLY/MONTHLY)' AFTER embedding_enabled,
    MODIFY COLUMN scope VARCHAR(20) NOT NULL DEFAULT 'ADMIN'
        COMMENT '설정 범위 (ADMIN/USER)',
    MODIFY COLUMN scope_key VARCHAR(100) NOT NULL DEFAULT 'GLOBAL'
        COMMENT '범위 키 (ADMIN=GLOBAL, USER=username)',
    ADD UNIQUE KEY uk_journal_setting_scope (scope, scope_key);
```
