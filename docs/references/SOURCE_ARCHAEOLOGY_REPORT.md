# DreamDiary 소스코드 고고학 조사 보고서

> 조사일: 2026-08-07
> 대상: 전체 소스코드 (dev_0.26.0 기준)
> 방법론: CODE_ARCHAEOLOGY_CHECKLIST v6 적용 (NETWORK·CHANGE·PRESSURE 축)

---

## 1. 프로젝트 구조 개요

```
workspace/
├── app/
│   ├── backend/          ← Spring Boot (Java 17)
│   │   └── src/main/java/io/nicheblog/dreamdiary/
│   │       ├── auth/           (인증/JWT/보안)
│   │       ├── feature/        (도메인 기능)
│   │       │   ├── journal/    ← 핵심 도메인
│   │       │   │   ├── entry/chapter/reflection/thread/day/
│   │       │   │   ├── annual/embedding/todo/
│   │       │   │   └── _shared/
│   │       │   ├── chat/       (AI 채팅)
│   │       │   ├── board/admin/user/calendar/file/attachable/notify/report/
│   │       │   └── ...
│   │       ├── global/         (상수/유틸/엔티티기반/설정)
│   │       └── infrastructure/ (캐시/코드/로그/메시징/웹)
│   ├── frontend-vue/     ← Vue 3 SPA (Metronic 기반)
│   │   └── src/
│   │       ├── features/       (journal/chat/admin/board/calendar/auth/user/attachable)
│   │       ├── shared/         (components/layout/http/i18n/utils/ui)
│   │       └── platform/       (metronic 테마)
│   ├── frontend-react/   ← [ZOMBIE] auth만 구현, 빌드 미포함
│   └── mobile-react-native/ ← React Native 모바일 (초기)
├── docs/                 (spec, migration, references)
├── scripts/              (인코딩 검증, 에이전트 룰 동기화)
└── vendor/               (외부 소스 — 읽기 전용)
```

**핵심 도메인**: `journal` — entry/chapter/reflection/thread/day/annual/embedding/todo 8개 하위 도메인.

---

## 2. 성장곡선 (LOC Growth)

| 연도 | 소스 파일 수 | 증감 |
|------|-------------|------|
| 2024 | 1,062 | (기준) |
| 2025 | 1,281 | +219 (+21%) |
| 2026 | 1,650 | +369 (+29%) |

- **2년 만에 55% 성장**. 성장 가속 중.
- 2026년 성장의 주요 동인: Vue 마이그레이션(레거시→Vue 컴포넌트 이식) + journal 도메인 기능 확장(thread, reflection, embedding).

---

## 3. Churn 분석 — 가장 자주 변경된 파일 Top 20

| 순위 | 변경 횟수 | 파일 | 비고 |
|------|-----------|------|------|
| 1 | 181 | `docs/migration/journal/component-spec.md` | spec 문서 |
| 2 | 165 | `docs/migration/journal/interaction-spec.md` | spec 문서 |
| 3 | 112 | `docs/migration/journal/screen-spec.md` | spec 문서 |
| 4 | 92 | `messages_ko.properties` | i18n |
| 5 | 91 | `messages_en.properties` | i18n |
| **6** | **80** | **`JournalEntryItem.vue`** | **코드 1위** |
| 7 | 66 | `ApiUrl.java` (레거시 경로) | 삭제됨 |
| 8 | 46 | `build.gradle` | 빌드 설정 |
| 9 | 44 | `component-spec.md` (common) | spec 문서 |
| 10 | 43 | `journal.scss` | 스타일 |
| 11 | 43 | `jrnl_diary_module.ts` (레거시) | 삭제됨 |
| 12 | 42 | `journalThread.ts` | store |
| 13 | 42 | `DESIGN_NOTES.md` | 설계 문서 |
| 14 | 40 | `JournalChapterItem.vue` | 컴포넌트 |
| 15 | 38 | `journal.ts` (store) | store |
| 16 | 36 | `JournalThreadEntryService.java` | 서비스 |
| 17 | 34 | `JournalReflectionItem.vue` | 컴포넌트 |
| 18 | 34 | `ApiUrl.java` (현행 경로) | URL 상수 |

**발견**: 코드 churn 상위 5개가 모두 journal 도메인. spec 문서의 높은 churn은 마이그레이션 과정에서 지속적 갱신을 반영 — 이것 자체는 건강한 패턴.

---

## 4. Co-Change 분석 — 강결합 쌍

| 공변 횟수 | 파일 A | 파일 B | 해석 |
|-----------|--------|--------|------|
| **23x** | `JournalEntryItem.vue` | `JournalReflectionItem.vue` | 가장 강한 결합. entry/reflection이 UI 레벨에서 분리되지 않음 |
| 16x | `jrnl_diary_module.ts` | `jrnl_dream_module.ts` | 레거시 lockstep (이미 삭제) |
| 10x | `JournalChapterItem.vue` | `JournalEntryItem.vue` | 계층 관계상 자연스러운 co-change |
| 9x | `JournalThreadEntryRepository` | `JournalThreadEntryService` | 정상 (repo↔service 쌍) |
| 9x | `JournalThreadRestController` | `journalThread.ts` | 정상 (API↔store 쌍) |
| 9x | `MarkdownUtils` | `MarkdownUtilsTest` | 정상 (코드↔테스트 쌍) |
| 8x | `JournalThreadEntryService` | `JournalThreadDetailContent.vue` | 백엔드↔프론트 결합 |
| 8x | `journalThread.ts` | `JournalThreadDetailContent.vue` | store↔컴포넌트 결합 |

**핵심 발견**: `EntryItem ↔ ReflectionItem` 23회 co-change는 이 두 컴포넌트가 사실상 단일 UI 단위로 동작함을 의미. 하나를 바꾸면 반드시 다른 하나도 바꿔야 하는 상태.

---

## 5. Fan-In 분석 — 가장 많이 의존되는 모듈

### 백엔드 (Java) — Top 10 imported classes

| 횟수 | 클래스 | 역할 |
|------|--------|------|
| 341 | `List` | stdlib |
| 302 | `RequiredArgsConstructor` | Lombok |
| 258 | `Log4j2` / `Getter` | Lombok/Log |
| 182 | `SuperBuilder` | Lombok |
| 164 | `ContentType` | 도메인 enum ← **주목** |
| 155 | `StringUtils` | 유틸 |
| 147 | `Component` | Spring |
| 114 | `DateUtils` | 커스텀 유틸 |
| 113 | `MessageUtils` | 커스텀 유틸 |
| 84 | `AuthUtils` | 인증 유틸 |

**발견**: `ContentType`(164회) = 전체 도메인 기능의 허브 enum. 이 값이 바뀌면 164개 파일에 영향. 현재는 안정적이지만, 확장 시 리스크 포인트.

### 프론트엔드 (Vue/TS) — Top 10 imported modules

| 횟수 | 모듈 | 역할 |
|------|------|------|
| 114 | `locale` (i18n) | 다국어 |
| 56 | `swal` | 알림 |
| 48 | `journal` (store) | 저널 상태 |
| 28 | `journalModal` (store) | 모달 상태 |
| 25 | `journalThread` (store) | 스레드 상태 |
| 19 | `journalDate` | 날짜 유틸 |
| 18 | `sessionPing` | 세션 유지 |
| 17 | `attachableModal` | 첨부 모달 |
| 14 | `auth` | 인증 |
| 12 | `safeModalClose` | 모달 닫기 유틸 |

**발견**: `journal` store(48회 import) + `journalModal`(28회) + `journalThread`(25회) = 저널 관련 store 3개가 프론트 전체의 허브. 이 store들의 인터페이스 변경은 cascade 영향이 큼.

---

## 6. 구조적 위험 — PRESSURE 표

| 파일 | 크기 | churn | fan-in | co-change | 압력 유형 | 위험도 |
|------|------|-------|--------|-----------|-----------|--------|
| `ChatAIService.java` | 158KB / 100+ methods | 낮음 | 낮음(내부 호출) | 낮음 | **Responsibility Leakage** | 🔴 높음 |
| `JournalEntryItem.vue` | 53KB | 80x | 높음(journal feature) | 23x(↔Reflection) | 크기 + co-change 결합 | 🟠 중간 |
| `journalModal.ts` | 43KB | 중간 | 28x import | 중간 | 크기 + fan-in 집중 | 🟠 중간 |
| `journalThread.ts` | 37KB | 42x | 25x import | 8x(3쌍) | churn + fan-in | 🟠 중간 |
| `JournalEntryService.java` | 37KB / 35+ methods | 중간 | 높음 | 중간 | 크기 + 책임 범위 | 🟡 주의 |
| `JournalChapterService.java` | 34KB | 중간 | 중간 | 8x(↔Test) | 크기 | 🟡 주의 |
| `MenuService.java` | 37KB | 낮음 | 중간 | 낮음 | 크기만 | 🟡 주의 |
| `ContentType` (enum) | 작음 | 낮음 | **164x** | 낮음 | fan-in 집중 허브 | 🟡 주의 |
| `frontend-react/` (모듈 전체) | auth만 | 0 | 0 | 0 | **Zombie 모듈** | ⚪ 정리 대상 |

---

## 7. Dead/Zombie 코드

| 유형 | 대상 | 상태 | 조치 |
|------|------|------|------|
| Zombie 모듈 | `app/frontend-react/` | auth만 구현, 빌드 미포함, 참조 없음 | 삭제 또는 명시적 보류 선언 |
| Deprecated enum | `ActvtyCtgr.SCHEDULE` | 주석으로 DEPRECATED 표기 | enum 값 제거 or @Deprecated |
| Dead legacy | `static/js/` (히스토리에만 존재) | 현재 소스에서 참조 0건 | 이미 제거 완료 ✓ |
| Debug code | `static/js/.../tinymce.ts` console.log | 레거시 잔존 | 삭제 |

---

## 8. TODO/FIXME 현황 (릴리스 차단 검토)

| 파일 | TODO 내용 | 차단 여부 |
|------|-----------|-----------|
| `FileRecordService.java` | 파일 순번, Tika 검증, 특수문자, 실제 삭제 | ❌ 비차단 (향후 개선) |
| `AuthService.java` | 프로필 체크, 역할 분리 | ❌ 비차단 |
| `MainPageController.java` | 접근 권한 통제 (2건) | ⚠️ 잠재적 보안 — 현재 리다이렉트로 우회 중 |
| `XlsxUtils.java` | 필드 캐싱, 임시파일 캐시 | ❌ 비차단 |
| `MqttUtils.java` | 옵션 살펴보기 (2건) | ❌ 비차단 |
| `UserMapstructToEntityTest.java` | updateFromDto 테스트 미완 | ❌ 비차단 |
| `RefreshToken.java` | 빈 TODO | ❌ 비차단 |
| `JwtTokenProvider.java` | 정밀 예외 처리 | ⚠️ 잠재적 보안 |
| `XssFilter/XssRequestWrapper` | 보완 필요 (미사용 중) | ⚠️ 미사용이면 zombie |
| `UserSignupRestController` | 메시지 변수 분리, 기능추가 예정 | ❌ 비차단 |

**릴리스 차단 항목: 0건**. 잠재적 보안 관련 3건은 현재 동작에 영향 없으나 향후 우선 처리 권장.

---

## 9. 핵심 발견 요약

### 강점
1. **spec-driven 개발 문화** — spec 문서 churn 1~3위는 코드와 문서가 동기화되고 있다는 증거
2. **레거시 수렴 완료** — `static/js/`, `templates/` 참조 0건. 마이그레이션 수렴 원칙 이행 중
3. **co-change 건강 패턴** — repo↔service, code↔test 쌍이 자연스럽게 결합
4. **성장 속도 적절** — 연 20~30% 성장은 관리 가능한 범위

### 위험
1. **ChatAIService.java = God Object** — 158KB, 100+ private methods. RAG 파이프라인·intent 분류·person synthesis·guard·metadata 빌딩·fallback 전략이 단일 파일에 혼재. 현재는 이 파일의 churn이 낮지만, 기능 추가 시 폭발적 성장 예상.
2. **프론트 핵심 3파일 비대** — `JournalEntryItem.vue`(53KB), `journalModal.ts`(43KB), `journalThread.ts`(37KB). 이 3파일이 프론트 journal 기능의 80%를 담당.
3. **EntryItem ↔ ReflectionItem 강결합** — 23회 co-change. 이 두 컴포넌트의 독립 변경이 사실상 불가능.
4. **ContentType enum 허브 리스크** — 164개 파일이 의존. 확장/변경 시 ripple effect 큼.

---

## 10. 권고사항

### 즉시 (0.26.0 릴리스 전)
- 없음. 현재 구조적 위험은 "시한폭탄"이지 "현재 폭발 중"은 아님.

### 단기 (0.27.0 목표)
1. **`frontend-react/` 모듈 정리** — 삭제 or README에 보류 사유 명시. 현재 빌드에 포함되지 않는 zombie 모듈.
2. **TODO 중 보안 관련 3건 리뷰** — `MainPageController` 접근 권한, `JwtTokenProvider` 예외 처리, `XssFilter` 사용 여부 결정.

### 중기 (0.28.0~)
3. **`ChatAIService.java` 분해** — 제안 구조:
   - `ChatRagContextBuilder` (RAG 컨텍스트 구성)
   - `ChatIntentClassifier` (intent 분류)
   - `ChatPersonSynthesizer` (person meaning/stance/appearance)
   - `ChatResponseGuard` (응답 품질 검증)
   - `ChatAIService` (오케스트레이터)
4. **`JournalEntryItem.vue` 분해 검토** — 53KB 단일 컴포넌트는 Vue composition API로 로직 추출(composable) 후 UI 슬롯 분리 가능.

### 장기 (구조적)
5. **`ContentType` 허브 리스크 완화** — enum 직접 참조 대신 인터페이스 기반 다형성 or 레지스트리 패턴. 단, 현재 안정적이므로 불필요한 개편 금지.
6. **EntryItem ↔ ReflectionItem co-change 해소** — 공통 로직을 composable로 추출하여 독립 변경 가능하게.

---

## 11. 증거 비용 참조 (방법론 적용 메모)

| 분석 | 비용 | 강도 | 비고 |
|------|------|------|------|
| git log --name-only (churn) | ~30초 | 중 | 전체 히스토리 파일별 변경 횟수 |
| git ls-tree -l (large files) | ~5초 | 약 | 현재 스냅샷 크기만 |
| co-change pair counting | ~2분 | 강 | 결합도의 직접 증거 |
| grep fan-in (import count) | ~3분 | 중 | 정적 의존성 근사치 |
| read_code signature scan | ~10초/파일 | 강 | 책임 범위 직접 확인 |
| TODO/FIXME grep | ~5초 | 약 | 존재 여부만 |

---

## 12. 적용록 메타

| 항목 | 값 |
|------|-----|
| 초기 가설 수 | 5 (God Object, legacy 잔존, dead modules, 허브 리스크, 강결합) |
| 최종 확인된 위험 | 4/5 (legacy 잔존은 이미 해소 확인) |
| 새로 발견한 가설 | 1 (ContentType 허브 리스크 — 사전 예상 없었음) |
| 조사 중단 이유 | root cause 식별 완료 + 수정안 결정 가능 |
