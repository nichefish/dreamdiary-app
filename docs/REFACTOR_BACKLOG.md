# 리팩토링 백로그

프로젝트 전반의 리팩터·병목 후보를 우선순위로 정리한 **상시 문서**다. 특정 에이전트/세션의 소유가 아니라 공용 백로그이므로(§11 멀티 에이전트), 누구든 항목을 착수·해결하면 이 문서의 현황과 수치를 갱신한다.

- 최근 실측: **2026-08-30** (기준선 2026-08-14 대비 델타 표기)
- 측정 방식: 파일 라인 수 + 알려진 백로그 대조 기반 스캔(정밀 per-file SRP 분석은 착수 시 별도 수행)
- 검증 환경 제약: 이 저장소 일부 셸은 gradle loopback 차단으로 빌드 미검증 → 착수 시 사용자 머신 `./gradlew buildFrontend` 필요

## 기준선 이후 해결됨 (재등록 불필요)

- **차트 청크 지연 로드** — `JournalDayMeta.vue`가 `await import("vue3-apexcharts")` + async CSS로 지연 로드 전환 완료. apexcharts가 초기 번들에서 제거됨.
- **백엔드 EAGER 페치** — 조사 후 철회. 저널 목록이 태그·댓글을 인라인 렌더하므로 로드가 정당하며, 단순 EAGER→LAZY는 OSIV+DTO 매핑으로 무효. 월간뷰가 무거우면 백엔드가 아니라 프론트 렌더(가상스크롤/페이지네이션) 이슈.
- **스토어 테스트 안전망** — 대체로 완료(전체 약 223 테스트).

## P0 — god 파일 분해 (가장 시급)

| 파일 | 현재 라인 | 델타 | 메모 |
|---|---|---|---|
| `app/frontend-vue/src/features/journal/entry/JournalEntrySearchPage.vue` | 1422 | 신규 급증 | 검색 + 일괄 태그 패널 + 태그 카테고리 선택 + 리플렉션 수정 배선이 혼재. **분해 1순위** (하위 컴포넌트/컴포저블 추출: 일괄 태그 패널, 태그 카테고리 로직, 리플렉션 모달 배선). 최근 급증분이라 ROI 최고. |
| `app/frontend-vue/src/features/chat/AppChat.vue` | 1869 | = | 프론트 최대. |
| `app/frontend-vue/src/features/admin/AdminPage.vue` | 1038 | 증가 | |
| `app/frontend-vue/src/features/journal/entry/components/JournalEntryItem.vue` | 997 | 증가(847→997) | 이전 추출(highlightKeywords) 후 재증식. 액션·복사 로직 추가 추출 후보. |
| `app/backend/.../feature/chat/service/ChatOrchestrator.java` | 1392 | = | 백엔드 최대. |
| `app/backend/.../feature/ai/person/PersonSynthesisHybridService.java` | 971 | = | |
| `app/backend/.../feature/journal/entry/service/JournalEntryService.java` | 834 | = | |
| `app/backend/.../feature/journal/embedding/service/JournalEntryEmbeddingQueueService.java` | 781 | 신규 상위 | |

god 파일 분해는 회귀 위험이 크므로 **커버리지 확보 후** 착수한다.

## P1

- **공유 API 클라이언트 수렴** — 스토어 HTTP를 `@/shared/api/client`(타입드 `AjaxResponse<T>`/`PageResult<T>` + 언랩)로 수렴. 현재 채택 9 스토어 / raw axios 38. **강제 이관하지 않고 touch 시 기회적 채택**(컨벤션: `docs/DEV_NOTES.md` §frontend SPA 구조). 고빈도·무테스트 스토어(chat·userAdmin·adminPage·journalAnnual·journalThreadDetail)는 이관 전 **테스트 선행**.
- **프론트 테스트 게이트화 (구조 이슈, 미해결)** — `npm run test`가 어떤 빌드 게이트에도 걸리지 않아 스펙이 조용히 썩는다. `build.gradle`/CI 변경을 수반하므로 **별도 승인** 후 진행.

## P2 — 위생

- **서술형 주석 부채** — "변경 전/후·기존에는·더 이상·제거했다" 등 과거 비교/부재 중심 주석 약 232건(2026-08-14 155 → 2026-08-30 232). CLAUDE.md §2는 현재 상태 긍정 서술을 요구한다. 파일을 만질 때 함께 정리한다(일괄 전역 치환 금지).

## 제외 (vendor성 소스)

`app/frontend-vue/src/platform/metronic/**`(예: `MenuComponent.ts` 1130, `LayoutService.ts`, `DomHelpers.ts` 528)은 Metronic 테마 소스이므로 vendor로 취급하고 리팩터 대상에서 제외한다. `vendor/`는 CLAUDE.md §7에 따라 수정 금지.