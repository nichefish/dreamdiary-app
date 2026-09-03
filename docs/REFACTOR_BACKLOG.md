# 리팩토링 백로그

프로젝트 전반의 리팩터·병목 후보를 우선순위로 정리한 **상시 문서**다. 특정 에이전트/세션의 소유가 아니라 공용 백로그이므로(§11 멀티 에이전트), 누구든 항목을 착수·해결하면 이 문서의 현황과 수치를 갱신한다.

- 최근 실측: **2026-08-30** (기준선 2026-08-14 대비 델타 표기)
- 측정 방식: 파일 라인 수 + 알려진 백로그 대조 기반 스캔(정밀 per-file SRP 분석은 착수 시 별도 수행)
- 검증 환경 제약: 이 저장소 일부 셸은 gradle loopback 차단으로 빌드 미검증 → 착수 시 사용자 머신 `./gradlew buildFrontend` 필요

## 기준선 이후 해결됨 (재등록 불필요)

- **차트 청크 지연 로드** — `JournalDayMeta.vue`가 `await import("vue3-apexcharts")` + async CSS로 지연 로드 전환 완료. apexcharts가 초기 번들에서 제거됨.
- **백엔드 EAGER 페치** — 조사 후 철회. 저널 목록이 태그·댓글을 인라인 렌더하므로 로드가 정당하며, 단순 EAGER→LAZY는 OSIV+DTO 매핑으로 무효. 월간뷰가 무거우면 백엔드가 아니라 프론트 렌더(가상스크롤/페이지네이션) 이슈.
- **스토어 테스트 안전망** — 대체로 완료(전체 약 223 테스트).
- **Canonical modify contract — Schedule tag 후처리 누락** — DIG-002에서 확인한 `ScheduleService.modify()`의 canonical `afterWrite()` 누락을 `7ed5bed22`에서 복원하고 `ScheduleServiceValidationTest.modifyRunsCanonicalTagPostProcessing` 회귀 테스트를 추가했다.
- **Canonical modify contract — User 전처리 누락** — DIG-002에서 확인한 `UserService.modify()`의 `preModify()` 누락을 `7ed5bed22`에서 복원하고 빈 허용 IP 정규화 회귀 테스트를 추가했다. 계정 등록·수정 공통 계약은 `docs/migration/admin/screen-spec.md`와 동기화했다.

## P0 — god 파일 분해 (가장 시급)

| 파일 | 현재 라인 | 델타 | 메모 |
|---|---|---|---|
| `app/frontend-vue/src/features/journal/entry/JournalEntrySearchPage.vue` | 1422 → **1161** | ✅ SP1·SP2a 완료 | 일괄 태그(SP1 → `useEntryBulkTag`)·태그 카탈로그(SP2a → `useSearchTagCatalog`) 추출 완료(-261줄, 컴포저블 2·테스트 13). **잔여 1161줄은 검색조건 상태·쿼리/URL 동기화·복사/내보내기·모달 배선이 서로 결합된 코어 — 추가 분해 시 의존 주입 폭증(복사/내보내기 ~14개, 액션바 자식 ~21 props)으로 거짓 캡슐화가 되어 보류.** |
| `app/frontend-vue/src/features/chat/AppChat.vue` | 1869 | = | 프론트 최대. |
| `app/frontend-vue/src/features/admin/AdminPage.vue` | 1038 | 증가 | |
| `app/frontend-vue/src/features/journal/entry/components/JournalEntryItem.vue` | 997 | 증가(847→997) | 이전 추출(highlightKeywords) 후 재증식. 액션·복사 로직 추가 추출 후보. |
| `app/backend/.../feature/chat/service/ChatOrchestrator.java` | 1392 | = | 백엔드 최대. |
| `app/backend/.../feature/ai/person/PersonSynthesisHybridService.java` | 971 | = | |
| `app/backend/.../feature/journal/entry/service/JournalEntryService.java` | 834 | = | |
| `app/backend/.../feature/journal/embedding/service/JournalEntryEmbeddingQueueService.java` | 781 | 신규 상위 | |

god 파일 분해는 회귀 위험이 크므로 **커버리지 확보 후** 착수한다.

**진행 기록 (JournalEntrySearchPage.vue, 2026-08-30)**: 깨끗하게 분리 가능한 두 관심사만 컴포저블로 추출했다 — SP1 일괄 태그(`useEntryBulkTag`, 커밋 935dd1d5f), SP2a 태그 카탈로그·라벨·정규화(`useSearchTagCatalog`, 커밋 84ff83399). DOM/템플릿은 무변경(§4), 각 단계 vue-tsc 0·프론트 전체 스위트 통과(gradle node 직접 검증). 나머지(복사/내보내기·대화형 태그 추가·검색 조건·모달)는 검색 상태와 강결합이라 컴포저블화하면 의존 주입만 늘어 응집을 해쳐 **여기서 종료**한다.

## P1

- **공유 API 클라이언트 수렴** — 스토어 HTTP를 `@/shared/api/client`(타입드 `AjaxResponse<T>`/`PageResult<T>` + 언랩)로 수렴. 현재 채택 9 스토어 / raw axios 38. **강제 이관하지 않고 touch 시 기회적 채택**(컨벤션: `docs/DEV_NOTES.md` §frontend SPA 구조). 고빈도·무테스트 스토어(chat·userAdmin·adminPage·journalAnnual·journalThreadDetail)는 이관 전 **테스트 선행**.
- **프론트 테스트 게이트화 (구조 이슈, 미해결)** — `npm run test`가 어떤 빌드 게이트에도 걸리지 않아 스펙이 조용히 썩는다. `build.gradle`/CI 변경을 수반하므로 **별도 승인** 후 진행.

## P2 — 위생

- **서술형 주석 부채** — "변경 전/후·기존에는·더 이상·제거했다" 등 과거 비교/부재 중심 주석 약 232건(2026-08-14 155 → 2026-08-30 232). CLAUDE.md §2는 현재 상태 긍정 서술을 요구한다. 파일을 만질 때 함께 정리한다(일괄 전역 치환 금지).

## 제외 (vendor성 소스)

`app/frontend-vue/src/platform/metronic/**`(예: `MenuComponent.ts` 1130, `LayoutService.ts`, `DomHelpers.ts` 528)은 Metronic 테마 소스이므로 vendor로 취급하고 리팩터 대상에서 제외한다. `vendor/`는 CLAUDE.md §7에 따라 수정 금지.
