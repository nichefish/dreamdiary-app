# Prefix Scope 설계

> 상태: PERSONAL/GLOBAL Scope 및 게시글 선택 수렴 완료
> 작성일: 2026-07-30  
> 적용 브랜치: `dev_0.25.0`

## 1. 목적

Prefix는 콘텐츠 제목 앞에 표시하는 단일 말머리다. 사용자가 자유롭게 여러 개 붙이는 태그나 여러 분류 축을 표현하는 Category Scheme이 아니다.

개인 저널과 게시판은 관리 권한이 다르지만 다음 핵심 계약을 공유한다.

- 관리 주체가 미리 등록한 평면 Prefix 목록이 있다.
- 콘텐츠는 그 목록에서 Prefix를 0개 또는 1개 선택한다.
- Prefix 이름·색상·정렬 순서·활성 상태를 관리한다.
- Prefix 목록과 콘텐츠의 선택 연결은 공통 영속 구조를 사용한다.
- 개인 관리 API와 게시판 관리 API는 권한 경계가 다르므로 하나의 범용 API로 합치지 않는다.

이 문서는 Prefix 목록의 소유 경계를 `PERSONAL | GLOBAL`로 확장하고, 게시판별 동적 Prefix를 `boardKey` 기반 GLOBAL Scope로 수렴시키는 목표 설계를 정의한다.

## 2. 용어

### Prefix

제목 앞에 표시하는 말머리 선택지다. 예: `[공지]`, `[질문]`.

- 하나의 PrefixScope에만 속한다.
- 같은 Scope 안에서 이름은 중복될 수 없다.
- 비활성화해도 기존 콘텐츠의 선택과 표시는 보존한다.

### PrefixScope

Prefix 평면 목록 하나의 소유·선택 경계다. 분류 Scheme이나 콘텐츠와 Prefix 사이의 Assignment 헤더가 아니다.

- `PERSONAL`: 특정 사용자가 특정 논리 content type에서 관리하는 목록
- `GLOBAL`: 애플리케이션 또는 관리자가 특정 논리 content type에서 관리하는 공용 목록

`GLOBAL`은 모든 content type이 하나의 목록을 공유한다는 뜻이 아니다. 사용자 개인 소유가 아닌 관리 목록이라는 뜻이다. GLOBAL Scope도 content type별로 독립적이다.

### Scope content type

Prefix 목록을 찾기 위한 논리 키다.

- 개인 저널 챕터 말머리: `JOURNAL_CHAPTER_DIARY`(일기 챕터), `JOURNAL_CHAPTER_NOTE`(노트 챕터)
- 개인 저널 엔트리 말머리: `JOURNAL_DIARY`, `JOURNAL_DREAM`, `JOURNAL_NOTE`
- 개인 저널 스레드 말머리: `JOURNAL_THREAD`
- 게시판: `board.board_key`

`prefix_content.ref_content_type`은 실제 attachable 콘텐츠 식별자이고, `prefix_scope.content_type`은 관리 목록을 찾는 논리 키다. 소비자 resolver가 실제 콘텐츠 문맥을 Scope 키로 변환한다. 게시판에서는 두 값이 모두 `boardKey`이므로 별도 매핑이 필요 없다.

챕터 말머리는 이 두 값이 서로 다르다. 챕터의 attachable 정체성(`prefix_content.ref_content_type`)은 챕터 유형과 무관하게 항상 `JOURNAL_CHAPTER`로 유지하지만, 말머리 목록 소유 경계(`prefix_scope.content_type`)는 소속 챕터 유형으로 나뉜다. 일기 챕터는 `JOURNAL_CHAPTER_DIARY`, 노트 챕터는 `JOURNAL_CHAPTER_NOTE`를 사용하고, DREAM·시스템 요약 챕터는 사용자 말머리를 허용하지 않는다. 서버 `JournalChapterService.resolveChapterPrefixScopeContentType`와 프론트 `journalModal.resolveChapterPrefixContentType`가 챕터 유형을 Scope 키로 변환한다. 엔트리 말머리도 소속 챕터 유형으로 Scope를 결정하는 것과 같은 패턴이다.

## 3. 확정된 카디널리티

```text
PrefixScope 1 ─── N Prefix
Content     0 ─── 1 Prefix
```

- 하나의 Scope는 Prefix 여러 개를 가진다.
- 하나의 Prefix는 Scope 하나에만 속한다.
- 하나의 콘텐츠는 Prefix가 없거나 하나만 있다.
- Prefix를 여러 개 고르는 MULTI 계약은 도입하지 않는다.
- 담당자·상태·프로젝트처럼 다른 축이 필요하면 태그 또는 별도 속성으로 설계한다.

## 4. Scope 정체성

### PERSONAL

PERSONAL Scope의 정체성은 다음 세 값이다.

```text
(scope_type=PERSONAL, user_id, content_type)
```

예:

```text
(PERSONAL, 12, JOURNAL_THREAD)
(PERSONAL, 12, JOURNAL_CHAPTER_DIARY)
(PERSONAL, 12, JOURNAL_CHAPTER_NOTE)
```

- `user_id`는 필수다.
- 사용자와 content type의 조합당 Scope는 하나뿐이다.
- 첫 Prefix 등록 시 Scope를 lazy 생성한다.
- 사용자 가입 시 빈 Scope를 미리 만들지 않는다.

### GLOBAL

GLOBAL Scope의 정체성은 다음 두 값이다.

```text
(scope_type=GLOBAL, content_type)
```

게시판에서는 `content_type`에 `board.board_key`를 사용한다.

예:

```text
(GLOBAL, NULL, NOTICE)
(GLOBAL, NULL, SUPPORT)
```

- `user_id`는 `NULL`이어야 한다.
- GLOBAL content type당 Scope는 하나뿐이다.
- 신규 게시판 등록 시 빈 Scope를 미리 만들지 않는다.
- 게시판 관리자가 첫 Prefix를 등록할 때 GLOBAL Scope를 lazy 생성한다.

## 5. 영속 구조

### prefix_scope

```text
id
scope_type       PERSONAL | GLOBAL
user_id          PERSONAL이면 사용자 PK, GLOBAL이면 NULL
content_type     개인 논리 타입 또는 boardKey
owner_key        COALESCE(user_id, 0) 생성 컬럼
audit columns
```

DB 불변식:

- `scope_type`은 `PERSONAL | GLOBAL`만 허용한다.
- PERSONAL이면 `user_id IS NOT NULL`이다.
- GLOBAL이면 `user_id IS NULL`이다.
- `UNIQUE(scope_type, owner_key, content_type)`로 Scope 중복을 막는다.
- `user_id`는 nullable FK로 `user.id`를 참조한다.

MariaDB의 UNIQUE는 `NULL`을 서로 다른 값으로 취급하므로 `UNIQUE(scope_type, user_id, content_type)`만으로는 GLOBAL 중복을 막을 수 없다. 따라서 `COALESCE(user_id, 0)` 기반의 비NULL 생성 컬럼을 유일키에 사용한다. 실제 사용자 PK는 양수라는 기존 계약을 전제로 한다.

### prefix

```text
id
scope_id
name
color
sort_order
active_yn
audit columns
```

DB 및 서비스 불변식:

- `scope_id`는 필수 FK다.
- 같은 Scope에서 Prefix 이름은 비활성 행을 포함해 중복될 수 없다.
- 조회 순서는 `sort_order ASC, id ASC`다.
- 사용 중인 Prefix는 삭제하지 않고 비활성화한다.

### prefix_content

```text
id
prefix_id
ref_id
ref_content_type
audit columns
deleted_at
```

- 콘텐츠의 Prefix 선택은 공통 attachable 연결에 저장한다.
- 콘텐츠 테이블에 소비처별 `prefix_id` FK를 추가하지 않는다.
- 콘텐츠당 활성 연결은 0개 또는 1개다.
- soft-delete 이력과 재선택 충돌 때문에 단순 DB UNIQUE 대신 공통 선택 upsert 로직이 단일 활성 연결을 보장한다.
- Prefix 선택 시 소비자 resolver가 기대 Scope를 확정하고, 서버가 `prefix.scope_id` 일치를 검증한다.

## 6. 게시판 계약

### boardKey의 역할

현재 게시판 구조에서 `boardKey`는 단순 표시 코드가 아니다.

- `board_post.content_type`
- 게시판 라우팅 키
- 태그 등 attachable 테이블의 `ref_content_type`
- 게시판 메뉴와 캐시의 식별 키
- GLOBAL PrefixScope의 `content_type`

따라서 `boardKey`는 게시판 생성 후 변경할 수 없는 영속 식별자다. 프론트의 수정 화면 read-only만 신뢰하지 않고 백엔드에서도 변경을 거부하며 구조화 로그를 남긴다.

동적 boardKey는 고정 시스템 ContentType과 충돌하면 attachable 참조가 모호해진다. 신규 게시판 등록 시 고정·예약 content type과의 충돌을 서버가 거부해야 한다.

### Scope 조회

일반 사용자의 게시판 Prefix 조회 흐름은 다음과 같다.

```text
boardKey
→ 유효한 board 조회
→ GLOBAL PrefixScope(scope_type=GLOBAL, content_type=boardKey) 조회
→ 활성 Prefix 목록 조회
```

- Scope가 없으면 등록된 Prefix가 없는 정상 상태로 보고 빈 목록을 반환한다.
- 활성 Prefix가 없으면 게시글 폼과 검색 화면에서 Prefix 선택기를 표시하지 않는다.
- 클라이언트가 임의의 GLOBAL content type이나 Scope ID를 직접 지정하게 하지 않는다.

### 관리

게시판 관리 화면의 해당 게시판 행에서 Prefix 관리 모달을 연다.

- 게시판 ID로 관리 요청을 받고 서버가 boardKey를 확정한다.
- 첫 Prefix 생성 시 해당 boardKey의 GLOBAL Scope를 lazy 생성한다.
- 조회·수정·활성 변경은 해당 GLOBAL Scope 소속 Prefix에만 허용한다.
- 일반 사용자는 활성 Prefix를 조회하고 선택할 수 있지만 GLOBAL Prefix를 관리할 수 없다.

### 게시판 간 공유

게시판끼리 하나의 PrefixScope를 실시간 공유하지 않는다.

- 게시판마다 `GLOBAL + boardKey`로 독립된 Prefix 목록을 가진다.
- 한 게시판의 Prefix 수정은 다른 게시판에 전파되지 않는다.
- 게시판 관리 API는 대상 게시판 ID로 boardKey를 확정한다.

이 결정은 한 게시판의 Prefix 수정이 다른 게시판에 예기치 않게 전파되는 것을 막고, 게시판별 동적 말머리라는 관리 문맥을 단순하게 만든다.

### 게시글 선택

- 게시글 등록·수정 payload는 nullable 단일 `prefixId`를 사용한다.
- 서버는 선택 Prefix가 `GLOBAL + boardKey` Scope에 속하는지 검증한다.
- 비활성 Prefix는 신규 선택할 수 없다.
- 기존 게시글이 가진 동일한 비활성 Prefix는 다른 필드 수정에서 유지할 수 있다.
- 최종 선택 저장은 `prefix_content(ref_id=boardPostId, ref_content_type=boardKey, prefix_id)`를 사용한다.
- 게시글 테이블은 Prefix 직접 FK를 갖지 않는다.

## 7. 개인 Prefix 계약

개인 저널 Prefix는 다음 계약을 사용한다.

- `/api/my/prefixes`는 로그인 사용자 자신의 PERSONAL Scope만 관리한다.
- 요청 가능한 content type은 서버가 명시적으로 허용한 개인 Prefix 대상만 받는다.
- 임의의 boardKey나 GLOBAL content type을 개인 API에 전달해 조회·수정할 수 없다.
- 개인 콘텐츠 선택 시 콘텐츠 소유권과 PERSONAL Scope 소유권을 모두 검증한다.

PERSONAL과 GLOBAL은 영속 모델과 Prefix 불변식만 공유한다. 관리 권한과 API 진입점은 합치지 않는다.

## 8. 구현 상태

- PrefixScope는 `PERSONAL | GLOBAL` 소유 유형과 정규화 유일키를 사용한다.
- 개인 PrefixScope는 `(PERSONAL, user_id, content_type)`이다.
- 개인 저널 소비자는 `prefix_content`를 사용한다.
- 챕터 말머리 목록은 소속 챕터 유형으로 분리한다. attachable 정체성은 `JOURNAL_CHAPTER`로 유지하고, 목록 Scope는 일기 챕터 `JOURNAL_CHAPTER_DIARY`·노트 챕터 `JOURNAL_CHAPTER_NOTE`로 나뉜다. DREAM·시스템 요약 챕터는 사용자 말머리를 갖지 않는다.
- 게시판은 `GLOBAL + boardKey`로 독립 Scope를 조회하고 첫 Prefix 등록 시 lazy 생성한다.
- 게시글도 `prefix_content(ref_id, ref_content_type=boardKey, prefix_id)`와
  `PrefixEmbed`를 사용한다.
- 개인 저널과 게시판의 등록·수정·검색·표시 경로가 이 계약을 사용한다.

`docs/migration/**`의 화면·인터랙션·컴포넌트 spec은 각 구현 SAVEPOINT의 실제 코드 상태에 맞춰 갱신한다.

## 9. DB 정합성

현재 스키마와 운영 데이터는 다음 조건을 만족해야 한다.

- Prefix 영속 테이블은 `prefix_scope`, `prefix`, `prefix_content`다.
- PERSONAL Scope는 사용자와 논리 content type 조합당 하나이며, GLOBAL Scope는 boardKey당 하나다.
- 개인 챕터 Scope는 `JOURNAL_CHAPTER_DIARY`와 `JOURNAL_CHAPTER_NOTE`로 분리한다.
- 활성 `prefix_content`는 콘텐츠당 최대 하나이며 선택한 Prefix의 Scope가 소비 문맥과 일치한다.
- 게시판 Prefix의 Scope content type과 `prefix_content.ref_content_type`은 같은 boardKey다.
- 시스템 요약·DREAM 챕터는 Prefix 연결을 갖지 않는다.

## 10. 삭제·비활성 계약

- Prefix 비활성화는 기존 `prefix_content`를 삭제하지 않는다.
- 콘텐츠 soft-delete는 복원 시 기존 선택을 보존하도록 `prefix_content` 연결을 즉시 제거하지 않는다.
- 게시판 비활성화는 GLOBAL Scope나 Prefix를 연쇄 삭제하지 않는다.
- 게시판 soft-delete 후에도 기존 게시글 이력을 위해 Scope와 Prefix를 보존한다.
- boardKey는 삭제 후에도 재사용하지 않는다.

## 11. 검증 기준

- 개인 Prefix 관리 대상 6개가 각각 독립 Scope를 조회한다.
- 게시판 관리 화면과 게시글 등록·수정·검색이 같은 `GLOBAL + boardKey` 목록을 사용한다.
- Prefix가 없는 소비 화면은 빈 선택기를 렌더하지 않는다.
- 비활성 Prefix는 신규 선택지에서 제외되며 기존 선택 표시는 유지된다.
- 관리 변경 후 해당 content type의 활성 선택지 캐시만 무효화한다.
- Scope 불일치와 비활성 Prefix 신규 선택은 서버가 거부하고 구조화 로그를 남긴다.

## 12. 비목표

- 다중 Prefix
- Prefix 계층 구조
- PERSONAL/GLOBAL 통합 관리 API
- 게시판 간 실시간 Scope 공유
- Scope ID의 클라이언트 노출
- 임의 content type에 Prefix를 허용하는 범용 설정 화면
