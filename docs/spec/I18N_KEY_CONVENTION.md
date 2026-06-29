# i18n 키 네이밍 컨벤션

## 설계 철학

### 단일 SSOT
`messages_ko.properties` / `messages_en.properties` 가 서버(Java)와 프론트(Vue) 모두의 단일 진실 원천이다.
- Java: `MessageUtils.getMessage(key)` 로 조회
- Vue: `/i18n/{locale}.json` API를 통해 flat map으로 수신 (`I18nCatalogController` → `i18nCatalogService.ts`)

### 핵심 원칙
1. **도메인 우선** — 키는 도메인부터 시작한다. 타입 프리픽스(`txt.`, `msg.`, `bs.tooltip.`)는 쓰지 않는다.
2. **타입 구분 없음** — 레이블이든 메시지든 같은 `MessageSource`에서 꺼낸다. 타입 인픽스(`msg`, `label`)는 정보량이 없다.
3. **약어 금지** — `rslt` → `result`, `regstr` → `registrant`, `atch` → `attach`, `mnth` → `month` 등 풀네임으로 쓴다.
4. **현재 사용처를 키에 박지 않는다** — 현재 journal 화면에서만 쓴다는 이유로 `journal.summary` 같은 이름을 주지 않는다. 나중에 다른 화면에서 써도 키가 거짓말이 되지 않아야 한다.

---

## 키 구조

```
{domain}.{sub-feature}.{name}
{domain}.{sub-feature}.{name}.tooltip
{domain}.{sub-feature}.{name}.label
```

### 도메인 목록
`common`, `auth`, `user`, `journal`, `admin`, `board`, `file`, `calendar`, `attachable`, `lifecycle`, `history`, `jandi`, `report`

---

## 네임스페이스별 규칙

### `common.*` — 재사용 공통 레이블/메시지
도메인을 특정할 수 없거나 여러 도메인에서 재사용되는 키.

```properties
common.save=저장
common.delete=삭제
common.search=검색
common.year=년도
common.month=월
common.day=일
common.count=개
common.title=제목
common.content=내용
common.tag=태그
common.comment=댓글
common.history=변경 이력
common.attach-file=첨부 파일
common.regist-date=등록일
common.registrant=등록자
common.hit-count=조회수
common.related-content=관련 콘텐츠
```

### `common.result.*` — 처리 결과 메시지
서버가 API 응답에 실어 보내는 처리 결과 문구.

```properties
common.result.success=처리에 성공했습니다.
common.result.failure=처리에 실패했습니다.
common.result.empty=결과가 없습니다.
common.result.exception=처리 중 오류가 발생했습니다.
common.result.access-not-authorized=조회 권한이 없습니다.
```

### `common.confirm.*` — 확인 다이얼로그
```properties
common.confirm.save=저장하시겠습니까?
common.confirm.delete=삭제하시겠습니까?
common.confirm.logout=로그아웃하시겠습니까?
```

### `common.error.*` — 공통 오류 안내 문구
```properties
common.error.access-denied=접근 권한이 없습니다.
common.error.forbidden=허용되지 않는 접근입니다.
```

### `{domain}.*` — 도메인 레이블/메시지
```properties
auth.login-required=로그인이 필요합니다.
auth.bad-credentials=아이디 또는 비밀번호가 일치하지 않습니다.
auth.account-locked=잠금 처리된 계정입니다.

user.id.duplicated=이미 사용중인 아이디입니다.
user.pw.mismatch=비밀번호가 일치하지 않습니다.

journal.day.not-found=해당 일자의 데이터가 없습니다.
journal.chapter.not-found=챕터를 찾을 수 없습니다.
```

### `.tooltip` 접미어 — 툴팁 텍스트
해당 키의 툴팁 버전. 항상 3단계 이상 키의 끝에 붙는다.

```properties
auth.login.username.tooltip=아이디를 입력하세요.
auth.login.sign-in.tooltip=로그인합니다.
common.save.tooltip=저장합니다.
common.delete.tooltip=삭제합니다.
```

### `.label` 접미어 — 엔티티 표시명
도메인 엔티티 자체의 이름(표시 레이블). 해당 도메인 네임스페이스와 구분하기 위해 `.label`을 붙인다.

**문제**: `journal.chapter`는 "journal 도메인의 chapter 서브 피처"를 가리키는 네임스페이스이기도 하고, "저널 챕터"라는 엔티티 표시명이기도 해서 의미가 중첩된다.

**해결**: 엔티티 표시명은 `.label`을 붙여 네임스페이스와 명확히 구분한다.

```properties
journal.day.label=저널 일자          # "JournalDay" 엔티티의 표시명
journal.chapter.label=저널 챕터      # "JournalChapter" 엔티티의 표시명
journal.diary.label=저널 일기
journal.dream.label=저널 꿈
journal.interpretation.label=저널 해석

# journal.day.* 는 JournalDay 서브 피처 네임스페이스
journal.day.not-found=해당 일자의 데이터가 없습니다.
journal.day.duplicate=이미 등록된 일자입니다.
```

### `exception.*` — 예외 클래스 매핑
`MessageUtils.getExceptionBundleMsg()`가 예외 클래스명을 자동 변환하여 조회하는 키.

**변환 규칙** (`MessageUtils.getExceptionNm()`):
1. 이너클래스 구분자 `$` → `.`
2. 각 세그먼트에서 `Exception` 접미어 제거 (남는 것이 없으면 유지)
3. PascalCase → kebab-case

```
AccessDeniedException          → exception.access-denied
EntityNotFoundException        → exception.entity-not-found
HttpClientErrorException$Forbidden → exception.http-client-error.forbidden
SQLGrammarException            → exception.sql-grammar
```

서브키는 상위 키에 `.{suffix}` 형태로 붙이며, 예외 생성자에 직접 전달한다:

```java
// 자동 조회 (no-arg 또는 null)
throw new EntityNotFoundException();

// 서브키 지정: "this.suffix" → "exception.{class-key}.suffix"
throw new EntityNotFoundException("this.to-delete");
// → "exception.entity-not-found.to-delete" 로 자동 조합
```

```properties
exception.entity-not-found=요청한 데이터를 찾을 수 없습니다.
exception.entity-not-found.to-read=조회할 데이터를 찾을 수 없습니다.
exception.entity-not-found.to-modify=수정할 데이터를 찾을 수 없습니다.
exception.entity-not-found.to-delete=삭제할 데이터를 찾을 수 없습니다.
exception.access-denied=접근 권한이 없습니다.
```

### `error.*` — HTTP 에러 페이지
```properties
error.404=페이지를 찾을 수 없습니다.
error.500=내부 서버 오류가 발생했습니다.
```

---

## 금지 패턴

| 금지 | 이유 | 대신 |
|---|---|---|
| `txt.user.name` | `txt` 접두어는 정보 없음 | `user.name` |
| `msg.user.created` | `msg` 접두어는 정보 없음 | `user.created` |
| `bs.tooltip.save` | 프레임워크 이름 노출 | `common.save.tooltip` |
| `view.cnfm.delete` | `view` 는 분류 기준 아님 | `common.confirm.delete` |
| `txt.sumry` | 약어 | `journal.closing` |
| `txt.atch-file` | 약어 | `common.attach-file` |
| `journal.chapter` (엔티티명) | 네임스페이스와 중첩 | `journal.chapter.label` |

---

## 변환 이력

`0.21.x` 에서 기존 `txt.*` / `msg.*` / `bs.tooltip.*` / `view.*` / `AbstractUserDetailsAuthenticationProvider.*` 구조를 현재 컨벤션으로 일괄 변환.

주요 변환 규칙:
- `txt.{domain}.{rest}` → `{domain}.{rest}`
- `bs.tooltip.{domain}.{rest}` → `{domain}.{rest}.tooltip`
- `msg.rslt.*` → `common.result.*`
- `msg.{domain}.*` → `{domain}.*`
- `view.cnfm.*` → `common.confirm.*`
- `AbstractUserDetailsAuthenticationProvider.*` → `auth.*` (kebab-case 변환)
