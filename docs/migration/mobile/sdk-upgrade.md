# 모바일 Expo SDK 업그레이드 분석

> 상태: **실행됨(부분)** — SDK 57 의존성 업그레이드는 적용 완료, gradle Node 참조 갱신은 후속. 실기기 스모크 대기.
> 모바일 앱은 Expo SDK 57(RN 0.86, React 19.2)로 업그레이드되었다. 아래 55→57 분석은 판단 근거로 보존한다.
> 화면 계약은 [`screen-spec.md`](./screen-spec.md), 실행·빌드는 [`run-guide.md`](./run-guide.md).

---

## SDK 55 → 57 영향도 분석 (2026-09-03)

### 배경

스토어 Expo Go는 최신 SDK(57)만 실행하므로 SDK 55 프로젝트를 열지 못한다. 실기기 확인은 EAS preview APK(SDK 자체 내장) 또는 SDK 55용 Expo Go로 가능하며, 최신 스택으로 가려면 프로젝트를 57로 올리는 선택지가 있다.

- 현재: Expo SDK 55 = React Native 0.83, React 19.2
- 목표: Expo SDK 57 = React Native 0.86, React 19.2 (React 불변)
- 경유: SDK 56 = React Native 0.85, React 19.2, Hermes v1 기본

### 리스크 판정 (근거 확인 완료)

| 항목 | 판정 | 근거 |
|---|:---:|---|
| expo-secure-store API | ✅ 안전 | SDK 56·57 변경 로그에 secure-store 변경 없음. 사용처(`src/auth/accessToken.ts`)의 `getItemAsync`/`setItemAsync`/`deleteItemAsync`는 안정 API |
| New Architecture 전환 | ✅ 해당 없음 | New Arch는 SDK 55부터 강제(`newArchEnabled` 제거, Legacy 사용 불가) → 앱은 이미 New Arch로 동작. 전환·플래그 조정 없음 |
| 타입 깨짐 | 낮음(게이트) | React 19.2 불변, RN만 0.83→0.86. 업글 후 `tsc --noEmit`가 검출 |
| 런타임 회귀 | 실기기 스모크 필요 | SDK 57은 56에서 breaking 없음, 56의 Hermes 메모리 회귀도 57에서 수정 → 57 직행. 스모크 중점: Android edge-to-edge·네비 전환·safe-area 여백 |

### 실제 접점 2개 (핵심)

1. **`expo/fetch`가 기본 `globalThis.fetch`로 (SDK 56)**
   - `src/api/client.ts`가 전역 `fetch` + `credentials:"include"`(쿠키 인증) + Authorization 헤더에 의존 → 이 변경이 닿는 유일한 앱 코드.
   - 회피책: `.env`에 `EXPO_PUBLIC_USE_RN_FETCH=1` → React Native 내장 fetch를 전역으로 유지하여 현재 쿠키 동작을 그대로 보존한다.

2. **Node 툴체인 하한 상승**
   - RN 0.85+(SDK 56)는 Node 20.19.4+ 요구. 저장소가 핀한 gradle Node는 20.11.1(하한 미달).
   - 로컬 `expo install`/prebuild와 `docs/DEV_NOTES.md`·[`run-guide.md`](./run-guide.md) §4·CLAUDE.md §8의 Node 참조를 20.19.4+로 올려야 한다. EAS 클라우드 빌드는 자체 Node라 무관.

### 앱이 사용하지 않아 무관한 SDK 56 breaking

`expo-file-system` copy/move async화, `@expo/dom-webview` 기본 WebView, `expo-router`↔`react-navigation` 디커플, `@expo/vector-icons`→`@react-native-vector-icons`, expo-calendar/contacts/media 재설계 — 앱은 이 중 아무것도 사용하지 않는다(탭 아이콘은 이모지, `@react-navigation`을 직접 사용, WebView·file-system·calendar·contacts·media 미사용).

### 종합

앱 소스 변경은 사실상 불필요하다. 필요한 것은 버전 범프, `.env` 한 줄(`EXPO_PUBLIC_USE_RN_FETCH=1`), Node 툴체인 상향뿐이다. v0.1 마감의 필수 요건은 아니며, 최신 스택으로 갈 때 별도 SAVEPOINT로 수행한다.

### 업그레이드 절차 (착수 시)

실행 위치: **로컬 PC 터미널**(`npm run start` 를 돌리던 그 셸), 작업 디렉터리 `app/mobile-react-native`. `expo install` 은 레지스트리 버전 해석·lockfile 기록·Node 20.19.4+ 가 필요하므로 로컬에서 수행한다(에이전트 셸에서 대행하지 않는다).

1. Metro 가 떠 있으면 `Ctrl+C` 로 종료한다.
2. Node 버전 확인 — `node -v` 가 `v20.19.4` 이상이어야 한다. 미만이면 Node 를 먼저 올린다.
3. `npx expo install expo@latest` — Expo 코어를 최신(SDK 57)으로 올린다.
4. `npx expo install --fix` — 나머지 의존성을 SDK 기대치로 정합한다(react-native-safe-area-context·react-native-screens 경고 포함). `--fix` 는 단독 명령이 아니라 `expo install` 의 옵션이다.
5. `.env`(및 커밋 대상 `.env.example`)에 `EXPO_PUBLIC_USE_RN_FETCH=1` 을 추가해 쿠키 기반 인증을 React Native 내장 fetch 로 유지한다.
6. gradle Node 핀(`build.gradle` `node.version`)과 문서(run-guide §4·`DEV_NOTES`·AGENTS.md §8)의 Node 참조를 20.19.4+ 로 갱신한다. 이 gradle Node 는 frontend-vue 빌드와 공유하므로 교차 영향을 확인한다.
7. `tsc --noEmit` 와 인코딩 게이트 통과를 확인한다.
8. 실기기 스모크: 쿠키 로그인·일/주/월 조회→상세·검색·Android edge-to-edge.

3~4 는 `package.json`/lockfile 을 바꾸는 로컬 전용 스텝이므로, 이 변경과 5~6 의 리포 편집을 **한 커밋(단일 SAVEPOINT)** 으로 묶는다.

### 실행 기록 (2026-09-03)

- `npx expo install expo@latest` + `npx expo install --fix` 적용: expo `^57.0.19`, react-native `0.86.3`, react `19.2.3`, expo-secure-store `~57.0.3`, react-native-safe-area-context `~5.7.0`, react-native-screens `~4.26.0`. `@react-navigation/*`·typescript 는 불변.
- 타입 게이트가 `tsconfig.json` 의 deprecated `baseUrl`(TS5101)을 검출 → 미사용 `baseUrl`·`paths`(`@/*` alias 미사용)를 제거해 해소.
- `.env.example` 에 `EXPO_PUBLIC_USE_RN_FETCH=1` 추가(쿠키 인증 보존).
- gradle Node 참조(20.11.1→20.19.4+) 갱신은 frontend-vue 공유 툴체인이라 이 변경에서 제외하고 후속 처리한다(프론트 `buildFrontend` 검증 필요).
- 남은 것: 실기기 스모크(§업그레이드 절차 8).

### 출처

- Expo SDK 57 changelog — https://expo.dev/changelog/sdk-57
- Expo SDK 56 changelog — https://expo.dev/changelog/sdk-56
- Expo SDK 55 changelog — https://expo.dev/changelog/sdk-55
- expo/fetch API (docs) — https://docs.expo.dev/versions/latest/sdk/expo/
- Upgrade Expo SDK walkthrough — https://docs.expo.dev/workflow/upgrading-expo-sdk-walkthrough/
