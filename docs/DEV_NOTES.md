# DEV NOTES

## 공통 인코딩 게이트

- `npm run check:encoding` → `scripts/check_encoding.py`. 실패 시 **해당 변경 묶음 전체 폐기·되돌림**(부분 통과 없음). `scripts/`에는 검증 외 자동 수정 도구를 두지 않는다(`scripts/README.md`).

---

## 문서 기록 원칙

- **남길 것**: 아키텍처·SSOT·마이그레이션 방향, 제거·합의된 단일 경로, 되돌리기 어려운 계약, 운영·보안상 필수 결정.
- **남기지 말 것**: 자잘한 리네임·한 줄 수정·매 커밋 단위 “변경 로그”용 `.md` 양산.
- **같은 주제·같은 마이그레이션**은 이 파일(또는 `DESIGN_NOTES.md`) 안에서 정리한다. Phase별로 파일을 새로 만들지 말고, 필요하면 **한 문서에 `### Phase N` 섹션**만 둔다.
- UTF-8 깨짐이 보이면 **일괄 스크립트·전역 치환으로 복구하지 말고** 해당 범위에서 작업을 중단한다. (연쇄 손상 방지.)

---

## 패키지 구조

- "패키지는 파일 묶음이 아니라 책임 경계다."
- `feature`는 사용자 시나리오와 비즈니스 규칙에만 집중한다.
- "`global` 상수는 `auth`/`feature`/`infrastructure`를 import하면 안 된다."
- "`feature`-`infrastructure` 계층간 협력은 구현체가 아니라 명시적 계약(포트/서비스)으로 연결한다."

---

## 저널 도메인 — 데이터·프론트 수렴

마이그레이션 철학·폴백 금지·UI 동결 등은 `CLAUDE.md` 를 본다. 여기서는 **구조 결정·경계**만 요약한다.

### 공통 원칙

- 목표는 **convergence**. 공통 축(부트스트랩·브리지·상태·렌더)을 세운 뒤 레거시 이중 경로를 제거하고 **단일 진입**만 남긴다. 브리지 실패 시 조용한 HBS 폴백으로 이어가지 않고 **로그 후 중단**이 기본이다.
- 데이터와 코드가 어긋나면 클라이언트 **땜빵** 없이 DB·시드·서버 단일 진실 원천을 맞춘다.

### 저장소: 저널 엔트리 하드컷

- 영속화는 **`journal_entry` 단일 테이블**로 수렴. 다형은 `content_type`(예: `JOURNAL_DIARY` 등)으로 구분.
- 스키마·수리 스크립트는 `src/main/resources/schema/` 등 저장소 내 마이그레이션 SQL 을 본다.

### 결산(Annual) — Vue·ESM (A-6 정리)

- **`dF.JournalAnnual` 표면**: `static/vue/feature/journal/annual/services/journalAnnualService.ts`(ES module) 단일. CRUD/Ajax 는 `journalAnnualCrudService`, 상태·렌더는 `journalAnnualStateService`(태그 헤더 3행·엔트리 리스트는 모두 Vue 브리지로 수렴 — Handlebars 직호출 0건).
- **목록 (`journal_annual_list`)**: `JournalAnnualListApp` 가 `#journal_annual_list_div` 소유. `journalAnnualCrudService.listAjax` → `window.JournalAnnualListVueApp.setList` — Handlebars 목록 카드 경로 제거.
- **Aside (`_journal_annual_aside_base`)**: `journalAnnualAsideService` + `JournalAnnualAsidePanelApp`; 년/월 변경은 `dF.JournalAnnualAside.yyMnth` 단일. SSR 부트스트랩은 `window.__journalAnnualAsideBootstrap`.
- **등록/리뷰 모달**: `JournalAnnualRegModalApp` / `JournalAnnualReviewRegModalApp`; FTL 가드 안에서 서비스 번들 순서 고정.
- **상세 (`journal_annual_dtl`)**: 페이지 부트는 `JournalAnnualDtlPageBoot` ES module 단일 수렴(A-7-α). 상단 카드·리뷰 목록은 `JournalAnnualDtlCardApp` Vue(A-7-β, `journalAnnualCrudService.dtlAjax` → `JournalAnnualDtlVueApp.setModel`). 태그 헤더(DAY/DIARY/DREAM 3행)는 `JournalAnnualEntryTagListApp` Vue(A-7-δ, `journalAnnualStateService.renderTagList` → `JournalAnnualEntryTagListVueApp.applyTagRow`). 엔트리 리스트(DIARY/DREAM)는 `JournalAnnualEntryListApp` + `JournalAnnualEntryItem` Vue(A-7-γ, `journalAnnualStateService.renderEntryList` → `JournalAnnualEntryListVueApp.setList`) — 행 컴포넌트는 `JournalEntryContent` / `JournalEntryContextMenu` / `JournalDayContextMenu` Vue 컴포넌트를 1:1 재사용하고, 좌열 일자 셀(stdrdDt + holyday/weather 표시)과 우열 댓글 등록·복사 버튼은 행 내부 인라인이다.
- **가시 dead**: 목록 헤더 검색이 호출하는 `dF.JournalAnnual.search()` 미정의 — 폴백 별칭 없이 유지(룰).
- **dead partial 정리(A-8)**: A-7 시리즈 직후, 호출 그래프상 사용처 0건으로 확정된 7개 HBS partial 을 일괄 제거했다. 대상: `_journal_day_stdrd_dt_partial`, `_journal_day_context_btn_partial`, `_journal_day_meta_btn_partial`, `_journal_entry_content_partial`(자기 자신 `_by_type` 래퍼 포함), `_journal_entry_context_btn_partial`(`_by_type` 래퍼 포함), `_journal_entry_copy_btn_partial`(`_by_type` 래퍼 포함), `_comment_reg_btn_partial`. FTL include 제거 페이지: `journal_annual_dtl`, `journal_day_cal`, `journal_day_meta`, `journal_day_weekly`, `journal_day_daily`, `journal_day_monthly`, `journal_entry_search`. 검증 기준은 ① Handlebars `{{> ... }}` 호출 0건(자체 `_by_type` 래퍼도 호출자 0건), ② JS/TS 문자열 참조 0건. 살아있는 인접 partial(`tag_list_partial`, `tag_list_sized_partial`, `comment_list_partial`)은 본 정리 범위 밖(A-9 에서 별도 처리).
- **일기/꿈 엔트리 태그 헤더 Vue 흡수(A-9)**: 일자(monthly/weekly/daily/cal/meta) + 엔트리 검색 페이지의 `#journal_diary_tag_list_div` / `#journal_dream_tag_list_div` 컨테이너가 placeholder 안내 박스(하드컷)로 비워져 있던 것을 Vue 로 수렴해 마무리했다. 신규 ESM `static/vue/feature/journal/day/JournalDayEntryTagListApp.ts` 가 두 컨테이너 위에 Vue 앱을 마운트하고 `window.JournalDayEntryTagListVueApp.setList(kind, list, { module })` 브리지를 노출(적재 경합은 `pendingByType` 큐잉으로 흡수). `journalEntryTagService.renderList` 는 `cF.handlebars.compile(..., "journal_entry_tag_list")` 컴파일 코드를 제거하고 본 브리지를 호출 — 서비스의 마지막 Handlebars 직호출이 사라졌다. `journalDayUiBridgeService.syncTagCloud` 는 `paintJournalEntryTagCloudHardCutNotice` 안내 박스 코드를 제거하고 `dF.JournalEntryTag.get(ct).listAjax()` 진입(=service.renderList → Vue 브리지 단일 경로). FTL 6개(`journal_day_monthly/weekly/daily/cal/meta`, `journal_entry_search`)에 본 ESM `<script type="module">` 적재를 추가하고 `_journal_entry_tag_list_template.hbs` / `_tag_list_partial.hbs` / `_tag_list_sized_partial.hbs` include 를 제거. 호출 그래프 검증(`{{>}}` 호출 0건, JS/TS 문자열 참조 0건) 후 3개 partial 파일을 삭제했다(결산 `journal_annual_dtl.ftlh` 의 `_tag_list_partial.hbs` include 도 동시 정리 — A-5-α 이후 이미 dead). 마크업/onclick 시그니처는 `_tag_list_sized_partial.hbs`(`<span class="py-2 me-3 cursor-pointer opacity-hover" onclick="{module}.select({id},'{tagNm}','{ctgr}')">…`) 와 1:1 동등.
- **dead partial 추가 정리(A-10)**: A-9 직후, "저널 Vue 통합 평가" 에서 식별된 6개 dead 의심 hbs partial 을 호출 그래프 1:1 검증 후 삭제. 대상: `_journal_entry_reg_btn_partial`(자기 자신 `_by_type` 래퍼 포함), `_journal_entry_toggle_btn_partial`, `_journal_entry_states_partial`, `_journal_tag_group_list_template`, `_comment_list_partial`, `_meta_list_partial`. 검증 기준은 A-8 과 동일 — ① Handlebars `{{> ... }}` 호출 0건, ② JS/TS 문자열로 id 참조 0건. 흡수처(이미 Vue 가 동일 마크업/onclick 동작을 보유): 등록 버튼/토글 버튼/상태 뱃지는 `JournalDayCard` / `JournalEntryItem` / `JournalChapterItem` / `JournalEntrySearchItem` / `JournalInterpretationItem`, 메타 sized 행은 `JournalDayMetaHeaderList`, `_comment_list_partial` 은 id `comment_list_partial` 자체 호출자 0건(별개의 id `comment_list` 모달은 `_comment_list_modal.ftlh` 가 보유, 본 정리 범위 밖). `_journal_tag_group_list_template` 은 `<#include>` / `{{>}}` / JS 참조 모두 0건이라 기존 시점부터 단순 dead. FTL include 제거 페이지: `journal_annual_dtl`, `journal_day_monthly/weekly/daily/cal/meta`, `journal_entry_search`, `journal_sbjct_reg_form`, 그리고 attachable 공용 `_comment_list_partial` 의 dead 정리 일관성을 위해 `board/notice/notice_regist_form` 의 dead include 도 동시 제거(저널 phase 범위 밖이지만 partial 삭제로 컴파일 깨짐 방지).

### 저널 일자(journal_day) — 목록·부트스트랩

- monthly/weekly/daily 목록은 `journalDayListAppMount` 및 `JournalDay*ListApp` 진입으로 `#journal_day_list_div` 를 소유한다. **단일 부트 축**으로 수렴.
- `dF.JournalDay.refresh`: Vue 마운트된 monthly/weekly 에서는 `JournalDayVueApp.refresh()` 로 reload 단일화.

### 어사이드·주간·필터·검색 파라미터

- 검색 파라미터·필터 상태 **단일 원천**으로 수렴. DOM 정렬·HBS 주입 폴백은 제거 방향.

### 런타임·`dF.JournalDay`

- 레거시 상태 저장소 역할은 전용 모듈로 분리하고, 파사드 최소 API·`dF.JournalDay` 참조 축소를 지속한다.

### 인라인 `onclick`·레거시 호출

- 이벤트 브리지·데이터 속성으로 옮기고 구역별 직접 호출을 정리해 **단일 액션 진입**만 남긴다.

### 태그 UI·모달

- Vue 분할·HBS 폴백 제거 방향. **TEXT_CLASS_CD / EMOTION** 등은 DB 시드가 단일 진실(UI 임시 옵션 금지).

### Vue 컴포넌트 디렉터리

- `journal/day/components` 에 섞였던 entry/chapter/interpretation 조립 컴포넌트는 `feature/journal/entry|chapter|interpretation/components` 로 직접 이동했다. **UI/DOM/클래스 불변.**

### 검증

- `npm run build:ts` 등. 브라우저 회귀: monthly/weekly·필터·주간 네비·태그·모달 등.

---

## 관리·공통 플랫폼 — Vue·URL·i18n

### UI 규약

- 화면 변경은 **명시 요청** 없으면 하지 않는다. 마이그레이션은 동작·마크업·클래스·플로우 보존.

### Vue 정적 리소스

- `static/vue/feature` 중심으로 경로 통일(예: admin/chat → `feature/admin`, `feature/chat`). 템플릿 스크립트 URL `/vue/feature/...` 정합.

### i18n 카탈로그

- `GET /i18n/{locale}.json` 단일 축, 비인증 접근 등 프록시 규칙 정합.

### RESTful·URL

- 태그·사용자·게시판·권한 등 API 스타일은 모듈마다 동일 원칙(리소스 중심, `.do` 정리 등). 세부 문자열은 코드·컨트롤러를 본다.

### Ajax 오류 처리

- 공통 실패 핸들러에서 XHR 메시지 노출 방식 정리.

### 페이지네이션·관리 UX

- 어드민·코드 등 페이지 번호 파서·FTL 기본값 **선행 순위** 규칙 정합.

### 게시판·코드·메뉴·공지·권한·사용자·로그

- 보드/post Vue, 코드 관리, 메뉴 관리, 공지·권한·사용자, 운영 로그 등은 **저널과 동형**으로 브리지 축소·컴포넌트화·단일 경로 수렴을 적용했다. DB 시드 순서 이슈는 **DB가 단일 진실**.

### 예약(enum)·사이트

- 구조적 예약 키는 enum 등으로 명시. 메뉴 라벨·캐시 무효화 정책은 운영 기대에 맞게 정리.

### 릴리즈 이력(`release_info`)

- 배포 단위 변경을 구조적으로 남긴다. 예: `SERVER_START`(매 기동), `DEPLOY`(릴리즈 식별 키 변경 시만). 초기화기에서 기록 실패가 부팅 전체를 막지 않게 예외 처리. 조회 API 예: `GET /cmm/get-release-history.do?size=20`.

### 메뉴 컬럼 하드컷 런북

- `menu` 테이블 컬럼명 하드컷·코드그룹 제거 등 **운영 DB 절차**는 별도 긴 런북이었으나, 본 저장소에서는 **실제 MariaDB 마이그레이션 스크립트·커밋 메시지**를 기준으로 검증한다(경로 고정 파일 없음).

### 검증

- `npm run build:ts` 및 화면별 회귀.

---

## 저널 일자(journal_day) 마이그레이션 — 문서 인덱스

- **도메인 의도**: `DESIGN_NOTES.md`
- **철학·규약**: `CLAUDE.md`
- **기술 롤업**: 아래 절.

---

## 저널 일자(journal_day) 마이그레이션 — 기술 변경 롤업

**규약·철학**은 `CLAUDE.md` 를 본다(중복 서술 생략).

### 1. 2026-05-07 — 모달·메타 페이지 Vue 이전

| 기록 | 한 줄 요약 |
|------|------------|
| 상세 모달 | HBS 제거 → `JournalDayDetailModalApp`, teleport, `JournalDayDetailVueApp` 큐 패턴 |
| 등록/수정 모달 | HBS 제거 → `JournalDayRegModalApp`, 플러그인·폼 서비스 브리지 |
| 메타 조회 모달 | HBS 제거 → `JournalDayMetaModalApp` / `JournalDayMetaVueApp` |
| 메타 페이지 설정 스트립 | `JournalDayMetaPageApp`, FTL/HBS 정리 |
| 검색 파라미터 SSOT | `getSharedSearchParams()` 등 접근 경로 단일화 |

### 2. 2026-05-08 — 검색·필터·폼·CRUD (Phase 1~6)

| Phase | 한 줄 요약 |
|-------|------------|
| 1 | `JournalDayListApp` 검색 파라미터 Vue reactive SSOT |
| 2 | CRUD·컨텍스트 메뉴 → Vue 소유, 레거시 `CrudService` 직접 호출 제거 |
| 3 | 등록 폼 TinyMCE/Tagify/datepicker·`regAjax` Vue 소유 |
| 4 | 이중 쓰기 제거, 레거시 get/patch → Vue SSOT 프록시 |
| 5 | 필터 핸들러 5종 Vue·`JournalDayVueApp` 노출 |
| 6 | 외부 호출자 `JournalDayVueApp` 직접 호출, 레거시 SearchStateService 축소 |

### 3. 2026-05-09 — Phase 9 ~ 17

| 구간 | 한 줄 요약 |
|------|------------|
| 9 | `journalDayUiBridgeService` 에서 `dF` 제거, URL·검색 SSOT 직결 |
| 10 | Vue에서 `JournalDayPageStateService` / `ViewService` 제거 |
| 11 | 레거시 데드코드·`runtime_service.refresh` 단순화 |
| 12 | aside/tag `getViewType()` 제거 → Vue SSOT |
| 13 | `JournalDayPageStateService` 완전 제거 |
| 14 | `JournalDayViewService` 일부 삭제·`.js` 동기화 |
| 15 | `JournalDayViewService` 삭제, `navigateToWeekDay` 브리지 |
| 16 | `refresh()` 외부 호출 제거·런타임 정리 |
| 17 | `JournalDayBootstrapService` 삭제 → `bootstrapDfJournalDayShell` |

### 4. 2026-05-09 — 소규모 슬라이스

- 태그 Vue 브리지 실패 시 침묵 제거 → DOM/`Swal`/`console`.
- 태그/메타 URL `yy` 폴백 제거: Vue SSOT만.
- 주간 네비 DOM 폴백 제거: 브리지 실패 시 경고·`console.error`.
- ListApp ↔ Aside 주간 네비: `syncAsideWeekNavigator` 직접.
- Aside `init` 순서: `initJournalDayAsideShell` 후 `JournalDayAside.init`.
- 엔트리 태그: `listEntryTagAjax` 제거 등 하드컷.
- `journalDayTagService` 브리지: `dF.JournalDayTag` 단일 위임.
- 목록 뷰 bootstrap: `journalDayListAppMount` 동기 구간 단일 호출.
- 룰: 비정상 감추 금지·메인 중간 깨짐 허용·새 폴백 금지 → `CLAUDE.md` 반영.

### 5. 목록 진입 3분할 + `journalDayListAppMount`

| 진입 모듈 | 역할 |
|-----------|------|
| `JournalDayMonthlyListApp.ts` | `mountJournalDayListApp("MONTHLY")` 만 호출 |
| `JournalDayWeeklyListApp.ts` | `await mountJournalDayListApp("WEEKLY")` 후 주간 전용 셸 |
| `JournalDayDailyListApp.ts` | `await mountJournalDayListApp("DAILY")` 후 일간 전용 셸 |
| `journalDayListAppMount.ts` | Vue·브리지·데이터 로드 SSOT(단일 구현) |

레거시 `journal_day_weekly.ts` / `journal_day_daily.ts` 의 `Page` 및 동명 `.js` 는 제거했다.

### 6. 일간 리소스 이름 (`journal_day_daily`)

| 항목 | 변경 |
|------|------|
| FTL | `journal_day_view.ftlh` → `journal_day_daily.ftlh` |
| 일간 진입 | `JournalDayDailyListApp.ts` — 레거시 `journal_day_daily.ts` / `Page` 제거 후 통합 |
| Spring 뷰 | `JournalDayPageController` → `…/journal_day_daily` |
| P3~P4 | 탭 `changeView` → `registerJournalDayViewService.js` + `journalDayUiBridgeService`. 런타임은 `journal_day_runtime_service.js` side-effect import. FTL에서 classic 서비스 스크립트 줄 제거. |

### 7. 운영 런북(메뉴 하드컷)

- 메뉴 DB 하드컷 런북은 **저널 일자 마이그레이션과 별개** 운영 절차로, 본 롤업에 포함하지 않음. 스크립트는 저장소 `schema`/마이그레이션을 본다.
