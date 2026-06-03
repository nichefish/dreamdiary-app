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

### 레거시 복원 모드 — UI 동일성 SSOT

- 저널 Vue 마이그레이션에서 화면 UI의 SSOT는 legacy templates/static의 partial, FTL, CSS, 실제 DOM이다. `app/frontend-vue` 구현은 이를 재해석하지 않고 먼저 동일하게 복원한다.
- 작업 순서: ① legacy partial/FTL 확인 ② legacy CSS 확인 ③ legacy 렌더 DOM·클래스 확인 ④ 현재 Vue 비교 ⑤ 차이 목록 작성 ⑥ 차이 전부 수정 ⑦ 타입체크와 필요한 spec 갱신.
- 사용자가 짚은 한 픽셀·문구·간격은 국소 요청이 아니라 해당 컴포넌트의 legacy 동등성 검수 신호로 취급한다. 그 지점만 고치고 끝내지 않는다.
- 의미를 이해하지 못한 UI는 추정·개선하지 않는다. 우선 legacy와 동일하게 옮긴 뒤, 개선은 별도 명시 요청이 있을 때만 진행한다.
- "현재 화면을 보고 적당히 비슷하게 맞춘 뒤 사용자가 발견한 차이만 수정"하는 방식은 실패다. 완료 보고 전 관련 범위의 legacy ↔ Vue DOM·클래스·스타일·동작 차이를 선제적으로 확인한다.
- 프레임워크 관용구로 마크업을 정리하지 않는다. legacy가 라벨과 코드를 별도 span/class/style로 나눴다면 Vue도 같은 DOM 경계와 class/style 경계를 유지한다. 클래스 통합, `fs-*` 통합, wrapper 합치기, gap 유틸 대체는 시각 차이를 만드는 재설계로 본다.
- 자동 비주얼 diff가 없는 범위에서는 완료 보고에 최소 검증 근거를 포함한다: 비교한 legacy 파일, 비교한 Vue 파일, 보존한 DOM/class/style 차이, 남은 미검증 범위. Playwright/스크린샷 diff 도입 전까지 이 항목은 수동 게이트다.

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
- **Vue 글로벌 `Message` 결의 통일(D)**: A-9 hotfix 가 드러낸 ESM 스코프 식별자 결의 race(`Cannot read properties of undefined (reading 'get')` first-render 시 가능)를 사전 차단하기 위해 공용 헬퍼 `static/vue/common/messageHelper.ts` 의 `resolveMessage(key, fallback?)` 를 도입했다. 결의 규칙: `window.Message` → `globalThis.Message` 우선 결의, `Message.get` 이 함수가 아니거나 throw 하면 `fallback ?? key` 반환(렌더 안전성 우선, 절대 throw 안 함). Vue 측 `Message.get(...)` 직호출 ~52건을 본 헬퍼 호출로 일괄 치환 — (1) template/computed 매 렌더 평가 4건(`JournalDayTagPanelApp` — `data()` 마운트 시점 1회 결의 후 캐시), (2) module top-level eval 7건(`journalEntryService` 의 `configs.contentLabel/emptyLabel` 4건 + `journalAnnualStateService` 의 `tagListConfigsCache` 3건), (3) `data()` 내 `t()` 헬퍼 9 + ModalBody computed 8건(annual 컴포넌트 일대), (4) `typeof Message !== "undefined"` 가드 6건(Aside 4 + `JournalDayList` + `JournalDayEntryTagListApp` 의 A-9 인라인 결의 함수도 헬퍼 위임으로 통일), (5) 이벤트 핸들러 안 `Swal.fire` 류 28+건(services 일대). API 사용은 두 가지: 단발 호출 `resolveMessage("...")` 또는 `data() { return { ... fooLabel: resolveMessage("...") } }` (마운트 시점 1회 결의 후 캐시). 호출 시그니처 보존 — 결의 결과는 변경 전 동작과 동일하며, 미정의 환경에서만 폴백이 다를 수 있다(이전: 미정의 시 ReferenceError 또는 `""`/`key`. 이후: 명시적 폴백 → `key` 반환이 기본).
- **Metronic Vue 데모 전환 — defer anchor**: 베이스 전환(`metronic_vue_v8.2.1_demo1/`) 은 잠재 종착지로 인지하되 즉시 진입은 보류한다(룰: convergence 우선, 동축 아닌 변경 묶음 금지, dual-path/coexistence 금지). 데모 스택은 Vue 3 + Vite + vue-router(`createWebHistory`, `base=/metronic8/vue/demo1/`) + Pinia + axios + vue-i18n + Element Plus, 단일 SPA `#app`, `assets/ts` 의 `MenuComponent.bootstrap()` 를 route 변경 시 `reinitializeComponents()` 로 재호출, JWT(`Authorization: Token ...`, `localStorage.id_token`) 가정. 현 시스템(Spring + FreeMarker per-page SSR + `_head.ftlh` 글로벌 적재 + 페이지별 `<script type="module" src="/vue/...">` ESM 진입 + 전역 `dF.*`/`cF.*` + 서버 messageMap 인라인 + tsc-only sass + form login + JWT filter + oauth2 + session IF_REQUIRED + remember-me) 과의 구조적 갭은 5개 축에서 동시 발생: 레이아웃(SPA 트리 vs FTL 셸), 라우팅(vue-router vs FTL/MVC), 인증(JWT/Token vs 세션+폼), i18n(vue-i18n 클라 카탈로그 vs 서버 인라인 messageMap), 빌드(Vite vs tsc-only). 부분 통합은 dual-path 가 강제되어 룰 위반. 진입 조건은 `시기` 가 아니라 `기능 상태` 4개(EC-1~EC-4) 가 동시 충족되는 시점으로 정의: EC-1 콘텐츠 영역 활성 `.hbs` partial 0(잔존은 호출 그래프상 명시 dead 만 — A-8/A-10 검증 기준: `{{>}}` 호출 0 + JS/TS 문자열 참조 0), EC-2 HBS/FTL 인라인 `onclick="dF.*"` / `onclick="cF.*"` 0건(`templates/` + `.hbs` grep 0), EC-3 페이지 = 단일 Vue root(현재처럼 한 페이지에 여러 ESM 진입이 흩어져 있지 않고 단일 셸 컴포넌트로 합쳐진 상태 — router-view 가 받을 prerequisite), EC-4 `dF.*` 가 Vue 서비스 단일 경로(`static/vue/feature/**/services/`) 로만 등록(잔존 `static/js/view/feature/**/*_module.js` 의 dF 부착 0). 부가 게이트(EC 와 별축): 그 SAVEPOINT 에서 `npm run build:ts` + `npm run check:encoding` 그린. EC 와 무관하게 **셸 phase 까지 살아있어도 무방한 자산** 3개(셸 phase 본 작업 범위): `cF.util/ajax/handlebars/format/validate` 공용 util(SPA 안에서도 노출 가능), `KTMenu.createInstances()` + Metronic v8.2.5 HTML 번들(`plugins.bundle.js` / `scripts.bundle.js`) (데모의 `MenuComponent` 모델로 일괄 이전이 셸 phase 본 작업), `_head.ftlh` 의 `window.Message` 서버 messageMap 인라인(셸 phase 안에서 i18n 재설계 결정). 진입 phase 진입 시 결정해야 할 sub-questions(키워드 anchor): ① 셸 흡수 범위(`layout_default` / `layout_with_aside` / `layout_without_sidebar` 모두 vs 일부), ② 라우팅(`createWebHistory` vs hash, Spring 정적 폴백 정책), ③ 인증(현 form+session 유지 vs JWT-localStorage 재설계 + CSRF 정책 — 현 `csrf().disable()` 정합 재검토), ④ i18n(서버 messageMap 인라인 유지 = JSON 카탈로그 export vs vue-i18n 단일 진실원천 이전), ⑤ 빌드(tsc-only 폐기 → Vite 도입; 산출물 위치·`/vue/...?releaseDate` 경로·`scripts/check_encoding.py` 룰 재정의 — SFC 안 한글 주석까지 검사 확장 필요), ⑥ 글로벌 `cF.*` / `dF.*` 처리(Pinia 흡수 vs 호환 shim 한시 — 룰의 dual-path 금지 와 충돌 안 하게 phase 안에서 종료 시점 명시), ⑦ Metronic KT* 처리(흩어진 `KTMenu.createInstances()` 호출을 데모 `MenuComponent` / `reinitializeComponents()` 모델로 일괄 이전). 시기 표현(예: "B/C phase 완료") 은 본문 anchor 에 직접 쓰지 않고 EC 충족이 보통 그 시점에 도달한다는 부가 사실로만 둔다. 향후 사용자가 "metronic vue 가자" 라고 명시하면 본 anchor 의 EC-1~EC-4 + sub-questions 7개를 곧장 plan 첫 단계로 꺼낸다.

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

### frontend-vue 패키지 구조 기준

- `src/components/` 는 앱 소유 공통 컴포넌트 루트다. `.gitignore` 로 숨기지 않는다. 시스템 공통 UI는 `components/system`, 폼·입력·표시 공통 UI는 `components/common` 아래에 둔다.
- `src/layouts/` 는 라우트 셸과 레이아웃 전용 하위 컴포넌트만 둔다. 전역 상태 패널, 입력기, 모달 버튼처럼 레이아웃 의미가 아닌 공통 UI를 `layouts/` 에 넣지 않는다.
- `src/views/` 는 라우트 화면과 feature-local 컴포넌트 경계다. 여러 feature가 재사용하는 컴포넌트는 `views/common` 에 새로 추가하지 말고 `src/components/common` 으로 둔다. 기존 `views/common/editor|tag` 는 이동 대상 부채다.
- `src/stores/` 는 Pinia 상태와 API 조립까지만 담당한다. store가 `views` 또는 DOM 컴포넌트를 import하면 구조 위반이다.
- `src/utils/` 는 순수 helper 또는 composable 성격만 둔다. 화면 렌더링 컴포넌트나 feature UI 상태가 커지면 `components` 또는 feature store로 이동한다.
- `src/vendor/` 와 `metronic_vue_v8.2.1_demo1/` 은 외부 원본/벤더 경계다. 앱 코드 정리 목적으로 내부 파일을 수정하거나 새 앱 컴포넌트를 추가하지 않는다.

### journal Vue 패키지 기준

- `views/journal/**` 는 Java `feature/journal/**` 패키지 경계를 따른다. `day`, `entry`, `chapter`, `interpretation`, `todo`, `annual`, `thread`, `shared` 를 기준으로 둔다.
- `daily`, `weekly`, `monthly`, `calendar`, `meta` 는 독립 feature가 아니라 `journal.day` 의 view mode/presentation 이다. 화면 파일은 `views/journal/day/` 아래에 둔다.
- `views/journal/day/components` 는 day aggregate 표시용 컴포넌트만 둔다. `JournalEntryItem`, `JournalChapterItem`, `JournalInterpretationItem` 처럼 다른 journal feature의 항목 컴포넌트는 각 feature 하위에 둔다.
- 여러 journal feature가 함께 쓰는 context menu, tag profile, comment/related modal 은 `views/journal/shared/**` 에 둔다.
- modal 위치도 대상 도메인을 따른다. 예: day 등록/상세/meta/tag 상세는 `day/modals`, entry 등록은 `entry/modals`, chapter 등록은 `chapter/modals`, todo 등록은 `todo/modals`.

### Metronic vendor 경계

- **Metronic asset**: Metronic에서 가져온 CSS/SCSS, 폰트, 이미지, 아이콘, 데모 미디어처럼 제품 코드가 아닌 정적 자산이다. 예: `src/vendor/metronic/assets/**`, `public/media/**`.
- **Metronic runtime/core**: 앱 코드가 직접 import하는 Metronic helper/plugin/service다. 예: `@metronic/core/services/ApiService`, `@metronic/core/plugins/keenthemes`, `@metronic/core/helpers/assets`. 이것은 정적 asset이 아니라 빌드 입력이지만, public repo에서는 Metronic 원본 재배포가 될 수 있으므로 커밋하지 않는다. 대신 로컬 설치/복원 절차를 둔다.
- **Metronic demo source**: 원본 데모의 샘플 Vue 컴포넌트와 샘플 화면이다. 예: `src/vendor/metronic/components/**`, `views/crafted/**`, `LayoutBuilder.vue`, 데모용 drawer/search/toolbar/modal 컴포넌트. 제품에서 쓰지 않으면 보관하지 않고 제거한다.
- **앱 소유 컴포넌트**: Metronic class, icon, asset을 사용하더라도 DreamDiary 라우트/레이아웃/기능에서 import하는 Vue 컴포넌트는 앱 소스다. `src/components/**`, `src/layouts/**`, `src/views/**` 아래 앱 경계에 둔다.
- 예외 판단: `layouts/default/components/modals/Modals.vue` 처럼 Metronic demo modal들을 조립하는 Vue 파일은 asset은 아니지만 현재 앱에서 import하지 않는 demo source다. 제품에서 필요해지면 `src/components` 또는 `src/layouts`의 앱 소유 경계로 새로 승격하고, import 경로를 실제 사용 vendor/app 컴포넌트에 맞춘 뒤 커밋한다. 쓰지 않으면 제거한다.
- 원칙: **추적되는 앱 코드가 import하는 파일은 ignored 상태로 두지 않는다.** ignored 파일을 import해야 한다면 먼저 그 파일을 앱 소유 코드로 승격하거나, 명시적 vendor 복원 절차를 만든다.
- public repo 원칙: Metronic 원본 소스·SCSS·폰트·이미지·데모 미디어는 git에 올리지 않는다. 필요한 경우 라이선스를 보유한 개발자가 로컬 vendor 복원 절차로 채운다.

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

---

## 2026-05-17 frontend-vue 마이그레이션 지시 누락 정리

이번 대화에서 나온 요구는 "화면을 Vue 파일로 만든다"가 아니라 **legacy templates/static 동작을 `app/frontend-vue`의 단일 앱 경계로 흡수한다**는 의미다. 중간 단계가 깨지는 것은 허용하지만, 완료라고 말하려면 아래 항목의 legacy 동등성을 검수해야 한다.

| 범위 | 결정/기록 |
|------|-----------|
| Metronic vendor 경계 | `vendor/metronic` 자체를 앱 루트로 만들려 하지 않는다. vendor 내부 링크를 전부 고치는 방향은 피하고, 앱 쪽 adapter/layout에서 감싼다. 장기적으로 `vendor/metronic/components`는 삭제 대상이다. |
| 기본 레이아웃명 | `layout/default-layout`은 의미 중복이므로 `layouts/default`로 둔다. |
| 첫 화면 메뉴 | 대시보드가 placeholder여도 기본 메뉴/사이드바가 있어야 한다. 이동 수단 없는 빈 화면은 마이그레이션 완료 상태가 아니다. |
| 사용자/관리자 메뉴 | 메뉴는 1차원 하드코딩이 아니라 `GET /api/menus?mode=USER|MNGR` + `subMenuList` depth 기반이어야 한다. fallback 메뉴는 서버 실패 시 보조 수단으로만 둔다. |
| 관리자 화면 | boardGroup, code, menu, users, logs, stats_user, auth-policy는 `/admin/**` Vue route로 들어온다. 각 화면은 legacy 기능 단위로 재검수한다. |
| 인증 결과 | `verify_success.ftlh`, `verify_failure.ftlh`는 `/auth/verify-result` 단일 Vue 화면으로 흡수한다. |
| attachable | 댓글/이력/관련글/태그/파일 등은 하나씩 Vue modal/store로 흡수하고 FTLH owner를 제거한다. |
| `static/vue/global` | 공통 전역은 `src/stores`, `src/utils`, `src/services`, `src/styles`로 재배치한다. 임시 참조가 남으면 migration spec에 제거 기준을 남긴다. |
| 폰트 | legacy `font.css`를 `/css/font.css`로 로드하고 `/font/**` 파일은 Spring Boot static 경로를 사용한다. Vite dev server는 `/css`, `/font` proxy를 둔다. |
| 저널 태그클라우드 | day/diary/dream tag row를 Vue에서 표시한다. "1개짜리 태그 숨김"과 "해당년도 태그 다 보기" 기능은 제거 요구에 따라 복원하지 않는다. |
| 태그 클릭 | 태그 클릭은 즉시 검색이 아니다. `JournalTagContextMenu`를 열고, 검색 액션에서 일자 태그는 상세 모달, 일기/꿈 태그는 새 창 검색으로 간다. |
| 엔트리 검색 새 창 | `/vue-app/journal/entry/search`는 legacy `journal_entry_search.ftlh`의 replacement다. 새 창이므로 메뉴/aside 없는 `SystemLayout` auth route로 둔다. 현재 구현은 목록/태그/키워드 조회와 결과 전체/개별 복사를 포함하며, export/sort/고급 다중 입력은 남은 검수 대상이다. |

문서 위치:
- 공통 통합 기준: `docs/migration/common/component-spec.md`
- 저널 태그/검색 흐름: `docs/migration/journal/component-spec.md`, `interaction-spec.md`, `screen-spec.md`
- 제품 동작 기준: `docs/JOURNAL_SCREEN_BEHAVIOR_SPEC.md`
