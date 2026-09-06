# 리팩토링 백로그

프로젝트 전반의 리팩터·병목 후보를 우선순위로 정리한 **상시 문서**다. 특정 에이전트/세션의 소유가 아니라 공용 백로그이므로(§11 멀티 에이전트), 누구든 항목을 착수·해결하면 이 문서의 현황과 수치를 갱신한다.

- 최근 실측: **2026-08-30** (기준선 2026-08-14 대비 델타 표기) · AppChat·AdminPage·UserAdminPage 분해 완료 반영 **2026-09-06**
- 측정 방식: 파일 라인 수 + 알려진 백로그 대조 기반 스캔(정밀 per-file SRP 분석은 착수 시 별도 수행)
- 검증 환경 제약: 이 저장소 일부 셸은 gradle loopback 차단으로 빌드 미검증 → 착수 시 사용자 머신 `./gradlew buildFrontend` 필요

## 기준선 이후 해결됨 (재등록 불필요)

- **차트 청크 지연 로드** — `JournalDayMeta.vue`가 `await import("vue3-apexcharts")` + async CSS로 지연 로드 전환 완료. apexcharts가 초기 번들에서 제거됨.
- **백엔드 EAGER 페치** — 조사 후 철회. 저널 목록이 태그·댓글을 인라인 렌더하므로 로드가 정당하며, 단순 EAGER→LAZY는 OSIV+DTO 매핑으로 무효. 월간뷰가 무거우면 백엔드가 아니라 프론트 렌더(가상스크롤/페이지네이션) 이슈.
- **스토어 테스트 안전망** — 대체로 완료(전체 약 223 테스트).
- **Canonical modify contract — Schedule tag 후처리 누락** — DIG-002에서 확인한 `ScheduleService.modify()`의 canonical `afterWrite()` 누락을 `7ed5bed22`에서 복원하고 `ScheduleServiceValidationTest.modifyRunsCanonicalTagPostProcessing` 회귀 테스트를 추가했다.
- **Canonical modify contract — User 전처리 누락** — DIG-002에서 확인한 `UserService.modify()`의 `preModify()` 누락을 `7ed5bed22`에서 복원하고 빈 허용 IP 정규화 회귀 테스트를 추가했다. 계정 등록·수정 공통 계약은 `docs/migration/admin/screen-spec.md`와 동기화했다.
- **AppChat.vue god 파일 분해** — 1532줄 SFC를 160줄 shell 오케스트레이터로 수렴하고, 기능 블록을 `features/chat/components/` 하위 8개 컴포넌트 + 공유 스타일 partial(`chatMessageRow.scss`)로 분리했다: ChatLauncher·ChatHeader·ChatEmptyState·ChatSessionBar·ChatComposer·ChatRagPanel·ChatMessageRow·ChatPendingRow. DOM·CSS 클래스·핸들러·모바일 @media·스크롤 컨테이너를 1:1 보존(§4)해 동작·레이아웃·인터랙션 무변경. 커밋 `c4ffbb28b`(SP1~SP4b 서브컴포넌트 추출을 squash). 빌드 검증은 사용자 머신 `./gradlew buildFrontend` 대기.
- **AdminPage.vue god 파일 분해** — 1038줄 SFC를 169줄 shell 오케스트레이터로 수렴하고, general/ai 탭 카드를 `features/admin/components/` 하위 6개 컴포넌트로 분리했다: AdminRoleCard·AdminDevToolsCard·AdminGeneralSettingsCard·AdminEntityQueueCard·AdminAiSettingsCard·AdminCacheModals. 공유 스타일은 `adminCards.scss` partial, 공용 숫자 포매터는 `adminFormat.ts` 로 수렴. DOM·CSS 클래스·핸들러·activeTab 게이트·bootstrap Modal lifecycle 을 1:1 보존(§4)해 동작·레이아웃 무변경. 카드 간 결합은 emit+defineExpose(캐시)·v-model(holydayYy)로 처리. 커밋 `d38dd8ac1`(SP1~SP5 squash). 빌드 검증은 사용자 머신 대기.
- **UserAdminPage.vue god 파일 분해** — 912줄 SFC를 139줄 shell 오케스트레이터로 수렴하고, 계정 관리 화면을 `features/admin/components/` 하위 3개 컴포넌트 + 공유 스타일 partial(`userAdminShared.scss`)로 분리했다: UserAdminDetailModal·UserAdminFormModal·UserAdminAccountList. DOM·CSS 클래스·핸들러·activeTab 게이트를 1:1 보존(§4)해 동작·레이아웃 무변경. 모달은 store 플래그(detailOpen/formOpen) v-if 구동이라 인스턴스 lifecycle 결합이 없고, 목록↔부모는 detail·edit 이벤트 위임으로 결합한다(openDetail/openEdit 는 딥링크·모달 바인딩 때문에 상위 유지). 커밋 `ddc5c0dfd`(SP1~SP3 squash). 빌드 검증은 사용자 머신 대기.

## P0 — god 파일 분해 (가장 시급)

| 파일 | 현재 라인 | 델타 | 메모 |
|---|---|---|---|
| `app/frontend-vue/src/features/journal/entry/JournalEntrySearchPage.vue` | 1422 → **1161** | ✅ SP1·SP2a 완료 | 일괄 태그(SP1 → `useEntryBulkTag`)·태그 카탈로그(SP2a → `useSearchTagCatalog`) 추출 완료(-261줄, 컴포저블 2·테스트 13). **잔여 1161줄은 검색조건 상태·쿼리/URL 동기화·복사/내보내기·모달 배선이 서로 결합된 코어 — 추가 분해 시 의존 주입 폭증(복사/내보내기 ~14개, 액션바 자식 ~21 props)으로 거짓 캡슐화가 되어 보류.** |
| `app/frontend-vue/src/features/journal/entry/components/JournalEntryItem.vue` | 950 | 감소(997→950) | 복사 tooltip·본문/리플렉션 조립·딥링크·결과 처리를 `useEntryCopy`로 추출하고 회귀 테스트 6개를 추가했다. 잔여 파일은 575줄 규모 템플릿과 모달 액션 배선이 큰 비중을 차지한다. **추가 분해 보류(2026-09-06)**: 로직은 이미 컴포저블 6종(useEntryCopy·Collapse·RelatedContent·LifecycleState·ThreadMembership·DayResolved)으로 분리 완료 — 리팩터 가치는 실현됨. 잔여 574줄 template 은 대부분 ⋯ 컨텍스트 메뉴 마크업이고, 최대 블록인 우측 액션 영역(~377줄)은 액션 전용 컴포저블 3종을 자식으로 통째 이동해도 `guardAxisWrite`·`scrollAfterFetch`·`refreshTagCloudAfterDelete` 콜백을 함수 prop 으로 내려야 해(≈8 props) `JournalEntrySearchPage` 와 같은 거짓 캡슐화가 된다. 마크업만 이동하고 배선 인터페이스가 늘어 이득 대비 간접화가 커 **여기서 보류**한다. 소형 표시 섹션(관련글·소속 스레드·댓글·꿈 태그 프로필)은 각 10~30줄이라 컴포넌트 오버헤드 대비 감소폭이 미미해 제외. |
| `app/backend/.../feature/chat/service/ChatOrchestrator.java` | 1392 | = | 백엔드 최대. |
| `app/backend/.../feature/ai/person/PersonSynthesisHybridService.java` | 971 | = | |
| `app/backend/.../feature/journal/entry/service/JournalEntryService.java` | 834 | = | |
| `app/backend/.../feature/journal/embedding/service/JournalEntryEmbeddingQueueService.java` | 781 | 신규 상위 | |

god 파일 분해는 회귀 위험이 크므로 **커버리지 확보 후** 착수한다.

**진행 기록 (JournalEntrySearchPage.vue, 2026-08-30)**: 깨끗하게 분리 가능한 두 관심사만 컴포저블로 추출했다 — SP1 일괄 태그(`useEntryBulkTag`, 커밋 935dd1d5f), SP2a 태그 카탈로그·라벨·정규화(`useSearchTagCatalog`, 커밋 84ff83399). DOM/템플릿은 무변경(§4), 각 단계 vue-tsc 0·프론트 전체 스위트 통과(gradle node 직접 검증). 나머지(복사/내보내기·대화형 태그 추가·검색 조건·모달)는 검색 상태와 강결합이라 컴포저블화하면 의존 주입만 늘어 응집을 해쳐 **여기서 종료**한다.

## P1

- **공유 API 클라이언트 수렴** — 스토어 HTTP를 `@/shared/api/client`(타입드 `AjaxResponse<T>`/`PageResult<T>` + 언랩)로 수렴. 현재 채택 14 스토어 / raw axios 33. **강제 이관하지 않고 touch 시 기회적 채택**(컨벤션: `docs/DEV_NOTES.md` §frontend SPA 구조). 지정 고빈도·무테스트 스토어 4종은 이관 전 **테스트 선행**으로 모두 완료: chat(46a6f4cd9)·userAdmin(2ae4f1eea→c2d72c96d)·adminPage(90f255a0c→4dd21d27b)·journalThreadDetail(159e41dce→a38fa1fdd)·journalAnnual(f16b2af3d→435af55bf). 잔여 raw axios 스토어는 touch 시 기회적 채택.
- **프론트 테스트 게이트화 (배선 완료, 그린 확인·머지 대기)** — `npm run test`(=`vitest run`)를 gradle `testFrontend` NpmTask로 감싸 `check` 라이프사이클에 연결하고(build.gradle), Jenkinsfile에 프론트 전용 `Test (Frontend)` 스테이지(`./gradlew testFrontend`)를 추가했다. 배포 경로(bootJar/processResources)에는 비결합. **선결**: 이 환경 loopback 차단으로 vitest 미실행 → 사용자 머신에서 `./gradlew npm_run_test` 그린 확인 후 CI 브랜치(`dev`)로 머지해야 게이트가 red로 시작하지 않는다. 백엔드 test는 현재 256중 84실패라 Jenkinsfile `Test (Backend)`는 계속 주석 유지(별도 triage).

## P2 — 위생

- **서술형 주석 부채** — "변경 전/후·기존에는·더 이상·제거했다" 등 과거 비교/부재 중심 주석 약 232건(2026-08-14 155 → 2026-08-30 232). CLAUDE.md §2는 현재 상태 긍정 서술을 요구한다. 파일을 만질 때 함께 정리한다(일괄 전역 치환 금지).

## 제외 (vendor성 소스)

`app/frontend-vue/src/platform/metronic/**`(예: `MenuComponent.ts` 1130, `LayoutService.ts`, `DomHelpers.ts` 528)은 Metronic 테마 소스이므로 vendor로 취급하고 리팩터 대상에서 제외한다. `vendor/`는 CLAUDE.md §7에 따라 수정 금지.
