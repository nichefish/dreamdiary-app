# 모바일 앱 화면 스펙 (React Native / Expo)

> 웹(Vue) 화면 스펙(`docs/migration/journal/screen-spec.md`)과 별도로 관리한다.
> 모바일은 네이티브 UX 패러다임(탭 내비게이션, 키보드 회피, 당겨서 새로고침 등)이 웹과 다르므로
> 화면 구성·인터랙션 기준을 따로 정의한다.

---

## 기술 스택

| 항목 | 내용 |
|---|---|
| 런타임 | Expo SDK 55 / React Native 0.83.2 / React 19.2.0 |
| 내비게이션 | React Navigation v7 (`@react-navigation/native-stack`, `@react-navigation/bottom-tabs`) |
| 상태 | React Context (`AuthContext`) |
| 인증 | JWT HttpOnly 쿠키 + SecureStore access JWT. REST는 쿠키와 Bearer를 함께 전송하고 WebSocket은 Bearer를 사용 |
| API 클라이언트 | `src/api/client.ts` — fetch wrapper (`credentials: include`) |
| 텍스트 유틸 | `src/utils/text.ts` — `stripHtml` (HTML 태그 제거) |

---

## 인증 흐름

```
앱 시작
  └─ AuthContext.mount
        ├─ setUnauthorizedHandler 등록 (401 → 토큰 삭제 + 자동 로그아웃)
        ├─ SecureStore access JWT → 메모리 hydrate
        └─ GET /api/auth/get-auth-account
              ├─ 성공(세션 있음) → access JWT refresh → isAuthenticated = true → MainTabs 진입
              ├─ 실패(401)      → 토큰 삭제 → isAuthenticated = false → LoginScreen 표시
              └─ 실패(403/5xx/네트워크) → authError 표시 → 다시 시도 버튼 제공

세션 만료 (API 401)
  └─ client.ts _onUnauthorized 콜백 호출
        → AuthContext setUser(null)
        → AppNavigator 가 isAuthenticated 변경 감지 → LoginScreen 자동 전환
```

로그아웃: `POST /api/auth/logout-json` → access JWT 삭제 → `setUser(null)` → LoginScreen 자동 전환

---

## 내비게이션 구조

```
RootStack (headerShown: false)
  ├─ Login       (비인증 상태)
  ├─ Main        (인증 상태)
  │     └─ MainTabs (하단 탭 5개)
  │           ├─ Today    → TodayScreen    (탭 레이블: "오늘" 📖, initialRouteName, QuickCapturePanel 내장)
  │           ├─ Calendar → CalendarScreen (탭 레이블: "달력" 📅)
  │           ├─ Tag      → TagExploreScreen (탭 레이블: "태그" 🏷️)
  │           ├─ Search   → SearchScreen   (탭 레이블: "검색" 🔍)
  │           └─ Profile  → ProfileScreen  (탭 레이블: "나"   👤)
  ├─ EntryDetail → EntryDetailScreen { entry, isDream }
  ├─ EntryEdit   → EntryEditScreen           { entry, isDream }  ← EntryDetail 에서 push
  ├─ InterpretationDetail → InterpretationScreen { entry }        ← EntryDetail(꿈) 에서 push
  ├─ AddEntry    → AddEntryScreen    { date }            ← TodayScreen FAB 에서 push
  └─ AiChat      → AIChatScreen                          ← QuickCapture/Profile/AddEntry 에서 push
```

**일반적인 탐색 경로**
- 기록 탭: TodayScreen → EntryDetail → EntryEdit (수정, pop(2)) / 삭제 후 goBack()
- 기록 탭: TodayScreen → AddEntry (+ FAB, 선택 날짜 기준, 저장 후 goBack())
- 달력 탭: CalendarScreen → 오늘 탭(`Today`, `{ date }`) → EntryDetail
- 달력 탭: 날짜 선택 → `navigateToDailyHub` → 오늘 탭에서 조회·FAB 추가

---

## 화면 목록

---

### 1. LoginScreen (`src/screens/LoginScreen.tsx`)

**상태**: 구현 완료

**목적**: 사용자명/비밀번호 로그인

| 요소 | 설명 |
|---|---|
| 앱 타이틀 | "DreamDiary" (accent 컬러 kicker) |
| 헤더 문구 | "다시 꿈으로" / "기록은 계속된다" |
| username 필드 | TextInput, autoCapitalize: none, autoCorrect: false |
| password 필드 | TextInput, secureTextEntry: true |
| 로그인 버튼 | `POST /api/auth/login` → 성공 시 AuthContext 갱신 → 자동 전환 |
| 오류 표시 | 버튼 아래 인라인 에러 텍스트 (`#C0392B`) |
| 로딩 상태 | 버튼 내 ActivityIndicator |

**UX 특이사항**
- KeyboardAvoidingView로 키보드가 올라올 때 입력 필드가 가려지지 않게 처리
- 로그인 성공 후 화면 전환은 AppNavigator가 `isAuthenticated` 변경으로 자동 처리 (`navigate()` 호출 불필요)

---

### 2. QuickCapturePanel / 빠른 기록 (`src/components/QuickCapturePanel.tsx`)

**상태**: 구현 완료 (Today 탭 내장, 입력 탭 제거)

**목적**: 꿈·감정·AI 빠른 캡처 — Daily 허브(오늘) 실행 축에 통합

| 요소 | 설명 |
|---|---|
| 배치 | `TodayScreen` — `atToday` 일 때만 표시 |
| 접힘 UI | `variant=compact` 기본: 「빠른 기록」 바 → 탭 시 펼침, 저장 후 접기 |
| 모드 선택 | 세그먼트 (꿈 / 감정 / AI) |
| 텍스트 입력 | chat: 안내 패널 / 나머지: multiline TextInput (compact minHeight 120) |
| 저장 | 오늘: `captureEntry(mode, text)` |
| AI | `rootNav.navigate("AiChat")` |
| 저장 후 | `onSaved` → Today 목록 refresh |

**`captureEntry` 파사드 흐름** (`src/api/dreamDiaryApi.ts`)
1. 오늘 JournalDay 조회(`GET /api/journal/days?viewType=DAILY`) → 없으면 생성(`POST /api/journal/days`)
2. 챕터 결정
   - `dream` 모드: `POST /api/journal/chapters/dream-auto?journalDayId={}` (없으면 자동 생성)
   - `emotion` 모드: 기존 비DREAM 챕터 재사용 또는 새 챕터 생성(`POST /api/journal/chapters`)
3. 엔트리 저장(`POST /api/journal/entries`, multipart/form-data)
   - `dream` → `contentType=JOURNAL_DREAM`
   - 나머지 → `contentType=JOURNAL_DIARY`

**UX 특이사항**
- 별도 Capture 하단 탭 제거 (`HomeScreen.tsx` 삭제)
- ProfileScreen 보조 진입: 「AI 대화」 버튼 → `AiChat` push
- 과거 날짜 입력: Calendar/Tag → 오늘 탭(해당 날짜) → FAB → AddEntry

---

### 3. TodayScreen / 오늘 (`src/screens/TodayScreen.tsx`)

**상태**: 구현 완료

**목적**: Daily 허브 — 날짜별 조회 + 오늘 빠른 기록 + FAB

| 요소 | 설명 |
|---|---|
| 헤더 | "DreamDiary" kicker + "오늘" 타이틀 |
| 날짜 네비게이터 | `‹` / `›` 버튼으로 하루씩 이동, 오늘 이후 `›` 비활성화 |
| 날짜 라벨 | 선택 날짜 (`YYYY.MM.DD`), 오늘 아닐 때 "오늘로" 힌트 → 탭하면 오늘 복귀 |
| 날짜 선택 | `useSelectedJournalDate` — 미래일 가드, AppState 복귀 재검증 |
| 당겨서 새로고침 | RefreshControl — accent 컬러 인디케이터 |
| DAILY 로드 | `useJournalDay(selectedDate)` — 포커스·pull refresh |
| 목록 UI | `JournalDayList` + `AddEntryFab` |
| 엔트리 탭 | `navigation.navigate("EntryDetail", { entry, isDream })` |
| 꿈 엔트리 스타일 | 보라색 배경(`#F5EEF8`), 텍스트(`#6C3483`), 좌측 테두리(`#8E44AD`) |
| 일반 엔트리 스타일 | `colors.surface` 배경, 기본 텍스트 컬러 |
| 빈 상태 | 오늘: "오늘은 아직 기록이 없습니다." + 「빠른 기록」/FAB 힌트 / 과거: "이 날의 기록이 없습니다." |
| 오류 상태 | 빨간 오류 텍스트. 서버 `message`를 우선 표시하고 조회 실패 시 직전 성공 일자 데이터를 유지 |
| 로딩 상태 | ActivityIndicator (large) |
| 빠른 기록 | `atToday` 시 `QuickCapturePanel` (접기/펼치기) |
| FAB (기록 추가) | 우하단 고정 원형 `+` 버튼 — 탭 시 선택 날짜 기준 `AddEntry` push |

**데이터 구조** (`GET /api/journal/days?viewType=DAILY&stdrdDt=YYYY-MM-DD`)
```
JournalDay
  ├─ journalChapterList[]
  │     ├─ journalDiaryList[]  → EntryCard (일반)
  │     ├─ journalDreamList[]  → EntryCard (꿈)
  │     └─ journalNoteList[]   → EntryCard (일반)
  └─ journalDreamList[]        → EntryCard (꿈, 챕터 외부)
```

**UX 특이사항**
- 챕터 레이블: `[카테고리명] 챕터제목` 형태
- 챕터 내 항목이 모두 비어있으면 ChapterSection 미렌더링
- 날짜 변경 → `useEffect` 재조회 (로딩 스피너 표시)
- 탭 포커스 → `useFocusEffect` 조용한 refresh (로딩 스피너 없음)
- FAB 탭 → `navigation.navigate("AddEntry", { date: selectedDate })` — 선택 날짜 기준 기록 추가. AddEntry 복귀 시 `useFocusEffect`로 자동 재조회됨

---

### 4. EntryDetailScreen / 엔트리 상세 (`src/screens/EntryDetailScreen.tsx`)

**상태**: 구현 완료

**목적**: 선택한 엔트리의 전체 본문 읽기

| 요소 | 설명 |
|---|---|
| 커스텀 헤더 | `‹ 뒤로` 버튼 + 우측 contentType 뱃지 (꿈/일기/노트) + 수정/삭제 액션 버튼 |
| 날짜 레이블 | `entry.stdrdDt` 있으면 `YYYY.MM.DD` 형태로 제목 위에 표시 (없으면 생략) |
| 제목 | `entry.title` (없으면 생략) |
| 본문 | HTML 태그 제거 후 전체 표시, fontSize 16, lineHeight 26 |
| 꿈 해석 버튼 | 꿈 엔트리일 때만 본문 하단에 표시 — 탭 시 `InterpretationDetail` push |
| 꿈 테마 | 배경 `#F5EEF8`, 본문 `#6C3483`, 헤더 borderBottom `#D7BDE2` |
| 일반 테마 | `colors.background` 배경, `colors.secondaryText` 본문 |

**내비게이션**
- `RootStack.EntryDetail` — params: `{ entry: JournalEntry; isDream: boolean }`
- `navigation.goBack()` 으로 이전 화면 복귀

---

### 5. CalendarScreen / 달력 (`src/screens/CalendarScreen.tsx`)

**상태**: 구현 완료

**목적**: 월별 달력으로 기록이 있는 날짜 시각화 및 날짜 선택 진입

| 요소 | 설명 |
|---|---|
| 헤더 | "DreamDiary" kicker + "달력" 타이틀 |
| 월 이동 | `‹` / `›` 버튼으로 월 이동, 미래 달은 `›` 비활성화 |
| 월 레이블 | `YYYY년 MM월` |
| 기록 일수 요약 | 로딩 완료 후 기록이 있는 날이 1개 이상이면 "N일 기록" 표시 (월 레이블 아래, 요일 헤더 위) |
| 탭 포커스 refresh | `useFocusEffect` — 오늘 탭에서 기록 추가 후 복귀 시 도트 갱신 |
| 요일 헤더 | 일 ~ 토 (일요일: 빨강 `#C0392B`, 토요일: 남색 `#1A5276`) |
| 달력 그리드 | 7열 × N행, 각 셀: 날짜 숫자 + 아래 도트 |
| 오늘 강조 | accent 컬러 원형 배경 (34×34), 흰 숫자 |
| 엔트리 도트 | 날짜 숫자 아래 5×5 원형 도트 (accent 컬러), 없으면 투명 |
| 미래 날짜 | 탭해도 이동 없음 (onPress guard: `if (!isFuture)`) |
| 범례 | 하단 "● 기록 있음" |
| 로딩 상태 | 그리드 영역 전체 ActivityIndicator |

**데이터 흐름** (`GET /api/journal/days?viewType=MONTHLY&stdrdDt=YYYY-MM-01`)
- 월별 JournalDay 목록 수신 → `journalChapterList`/`journalDreamList` 비어있지 않으면 도트 표시
- 월 변경 시 재조회, 탭 포커스 시 재조회
- API 오류 시 해당 월의 도트를 정상 빈 결과로 표시하지 않고 오류 문구와 `다시 시도` 버튼을 표시한다. 마지막 성공 월 데이터는 상태에 보존하지만 선택 월과 일치할 때만 도트 계산에 사용한다.

**날짜 탭 → Daily 허브 이동**
- `navigateToDailyHub(navigation, date)`가 날짜를 검증·오늘 이하로 보정한 뒤 `Today` 탭에 `{ date }`를 전달한다.
- 오늘 포함 과거 날짜만 탭 가능하다.
- API 오류 시 달력 날짜 그리드는 유지하고 오류 문구와 재시도 동작을 제공한다. 도트는 조회 성공이 확인된 월에만 표시한다.

---

### 6. EntryEditScreen / 기록 수정 (`src/screens/EntryEditScreen.tsx`)

**상태**: 구현 완료

**목적**: 기존 엔트리의 제목과 본문을 수정

| 요소 | 설명 |
|---|---|
| 헤더 | 좌: "취소" 버튼 / 중: "기록 수정" / 우: "저장" 버튼 (비활성 시 opacity 0.4) |
| 제목 입력 | 단일 행 TextInput, 선택 사항 |
| 구분선 | 제목/본문 사이 1px 선 |
| 본문 입력 | 다중 행 TextInput, minHeight 300, HTML 태그 제거 후 초기값 설정 |
| 저장 버튼 | `PUT /api/journal/entries/{id}` (FormData) → 성공 시 `navigation.pop(2)` |
| 로딩 상태 | 저장 버튼 내 ActivityIndicator |
| 오류 표시 | 빨간 박스 인라인 에러 |
| 꿈 테마 | 배경 `#F5EEF8`, 강조 컬러 `#8E44AD` |

**내비게이션**
- params: `{ entry: JournalEntry; isDream: boolean }`
- 저장 성공: `navigation.pop(2)` → EntryDetail + EntryEdit 2단계 pop → 목록 화면으로
- 취소: `navigation.goBack()` → EntryDetail 복귀

**주의**
- 백엔드 HTML 본문을 `stripHtml`로 평문 변환해 편집창에 노출.
  저장 시 평문이 전송되므로 기존 HTML 서식은 소실된다. (모바일 편집 한계)

---

### 7. AddEntryScreen / 기록 추가 (`src/screens/AddEntryScreen.tsx`)

**상태**: 구현 완료

**목적**: 달력에서 선택한 과거 날짜에 새 기록 추가 (오늘이 아닌 날짜 지원)

| 요소 | 설명 |
|---|---|
| 헤더 | 좌: `‹ 달력` 백버튼 / 중: "기록 추가" + 날짜 (`YYYY.MM.DD`) |
| 모드 선택 | 세그먼트 컨트롤 (꿈 / 감정 / AI 대화) |
| 텍스트 입력 | chat 모드: 안내 패널(`chatHint`) 표시 / 나머지: multiline TextInput, minHeight 200, 모드별 placeholder |
| 버튼 텍스트 | chat 모드: "AI 대화 시작" / 나머지: "저장" |
| 저장 버튼 | chat 모드: `navigation.navigate("AiChat")` 이동 / 나머지: `captureEntryForDate(mode, text, date)` 파사드 호출 |
| 저장 결과 | 성공 → 초록 박스 "저장했습니다." / 실패 → 빨간 박스 오류 메시지 |
| 로딩 상태 | 저장 중 ActivityIndicator |

**내비게이션**
- params: `{ date: string }` (YYYY-MM-DD)
- chat 모드: `navigation.navigate("AiChat")` — 바로 AI 대화 화면으로 이동
- 저장 후 `goBack()` → 오늘 탭(선택일 유지)에서 `useFocusEffect` refresh

**`captureEntryForDate` vs `captureEntry`**
- `captureEntry`: 오늘 날짜로 `captureEntryForDate` 호출하는 래퍼
- `captureEntryForDate`: 지정 날짜의 Day → 챕터 → Entry 3단계 파사드

---

## 공통 사항

### 테마 (`src/theme/colors.ts`)
웜 브라운 계열 팔레트.

| 토큰 | 용도 |
|---|---|
| `colors.background` | 화면 배경 (`#F7F2EA`) |
| `colors.surface` | 카드 배경 (`#EEE5D8`) |
| `colors.input` | TextInput 배경 (`#FFFDF9`) |
| `colors.border` | 구분선, 카드 테두리 (`#D7C9B8`) |
| `colors.text` | 기본 텍스트 (`#201B16`) |
| `colors.secondaryText` | 보조 텍스트 (`#6D6256`) |
| `colors.muted` | 흐린 텍스트 (`#9A8F83`) |
| `colors.accent` | 강조 브라운 (`#6E4C2F`) |
| `colors.onAccent` | accent 위 텍스트 (`#FFF8EF`) |

### API 클라이언트 (`src/api/client.ts`)
- Base URL: `API_BASE_URL` (환경 변수 또는 `src/config/env.ts` 설정)
- 모든 요청: `credentials: "include"`로 JWT 쿠키를 첨부하고, 메모리 access JWT가 있으면 `Authorization: Bearer ...`도 전송
- 로그인·refresh 성공 응답의 `Authorization` 헤더는 `expo-secure-store`와 메모리 캐시에 저장하며, 앱 시작 시 hydrate한 뒤 인증 계정 확인 후 refresh
- FormData 전송 시 Content-Type 헤더 생략 (fetch가 boundary 자동 설정)
- query 옵션의 값이 string[]이면 key[0]=v0&key[1]=v1 형태로 변환 (Spring MVC List 바인딩 대응)
- 401 → `_onUnauthorized` 콜백 호출 → AuthContext 자동 로그아웃
- 403 remains an ApiError/access-denied response and must not invoke `_onUnauthorized` or automatic logout.
- HTTP 오류는 경로·상태·응답 본문을 콘솔에 기록하고, 구조화된 응답의 비어 있지 않은 `message`를 `ApiError.message`로 사용한다. 서버 메시지가 없을 때만 상태코드가 포함된 공통 요청 실패 문구를 사용한다.
- Today·검색·태그·해석·채팅 조회 catch는 오류 컨텍스트를 콘솔에 기록하고 `ApiError.message`를 화면 오류 상태에 전달한다. 같은 조회 조건의 실패는 직전 성공 데이터를 빈 배열로 덮지 않는다. 태그 조건 전환처럼 이전 결과가 다른 조건의 데이터인 경우에는 결과를 초기화하되 오류 상태를 함께 표시해 실제 0건과 구분한다.

### 텍스트 유틸 (`src/utils/text.ts`)
- `stripHtml(html)`: HTML 태그 + `&nbsp;` 제거, trim
- TodayScreen, EntryDetailScreen 등에서 공유

### 날짜 유틸 (`src/utils/date.ts`)
- `toDateStr(date)`: Date → `YYYY-MM-DD`
- `parseDateOnly(dateStr)`: `YYYY-MM-DD` 문자열을 로컬 Date로 파싱
- `normalizeDateStr(dateStr)`: 형식/실재 날짜를 검증해 유효한 `YYYY-MM-DD`만 통과
- `addDays(dateStr, delta)`: 날짜 문자열 기준 일수 이동
- `formatDateDots(dateStr)`: `YYYY.MM.DD` 화면 표기
- 목적: `new Date("YYYY-MM-DD")`의 UTC 파싱으로 생길 수 있는 일자 오프셋을 방지해 Today/Day/AddEntry 날짜 흐름을 안정화

### 날짜 파라미터 가드
- `CalendarScreen`: `navigateToDailyHub` 호출 전 `dateKey`를 `normalizeDateStr`로 검증
- `CalendarScreen`: 월 도트 집계 시 API 응답 `stdrdDt`도 `normalizeDateStr`로 정제 후 사용
- `AddEntryScreen`: route의 `date`를 `normalizeDateStr`로 검증 후 사용 (비정상 값은 today로 fallback)
- `captureEntryForDate`: 저장 직전 날짜를 다시 검증하고, 비정상 날짜는 즉시 에러 반환
- `TodayScreen`: `useSelectedJournalDate` — AppState `active` 복귀 시 `clampDateToToday`
- `getDailyJournalDay` / `getMonthlyJournalDays` / `createJournalDay`: API 호출 전 날짜/연월 형식을 선검증해 비정상 요청 차단

### 인코딩
파일 전체 UTF-8(BOM 없음). 한글 주석·레이블 포함.


### 8. ProfileScreen / 내 정보 (`src/screens/ProfileScreen.tsx`)

**상태**: 구현 완료

**목적**: 로그인된 사용자 정보 조회 및 로그아웃

| 요소 | 설명 |
|---|---|
| 헤더 | "DreamDiary" kicker + "내 정보" 타이틀 |
| 아바타 | 닉네임 첫 글자 대문자, accent 컬러 원형 배경 (68×68) |
| 닉네임 | `user.nickname` |
| 사용자명 | `@user.username` |
| 이메일 | `user.email` (있을 때만 표시) |
| AI 대화 버튼 | `rootNav.navigate("AiChat")` push |
| 로그아웃 버튼 | Alert 확인 후 `AuthContext.logout()` → 자동 LoginScreen 전환 |
| 이번 달 통계 | 기록 일수 / 꿈 기록 / 일기 카운트 — `getMonthlyJournalDays` 호출 후 집계 |
| 통계 로딩 | ActivityIndicator (통계 카드 내). 실패 시 숫자를 0으로 표시하지 않고 서버 오류 메시지와 `다시 시도` 버튼 표시 |
| 로딩 상태 | 로그아웃 중 ActivityIndicator (버튼 내) |

**내비게이션**
- 하단 탭 "나" (5탭 중 마지막, `👤` 이모지 아이콘)
- 로그아웃 성공 → `AppNavigator`가 `isAuthenticated` 변경 감지 → LoginScreen 자동 전환
- `useFocusEffect` — 탭 포커스 시 이번 달 통계 재로드 (오늘 탭 저장 후 복귀 등)


### 9. TagExploreScreen / 태그 탐색 (`src/screens/TagExploreScreen.tsx`)

**상태**: 구현 완료

**목적**: 웹 주간/월간 목록 상단 태그 클라우드를 모바일 전용 탐색 탭으로 분리

| 요소 | 설명 |
|---|---|
| 헤더 | "태그" + 부제 "이번 달 태그로 기록을 찾습니다" |
| 월 이동 | `‹` `›` — Calendar 와 동일 패턴, 미래 월 비활성 |
| 섹션 | 일자 / 일기 / 꿈 태그 3행 (칩 + contentSize) |
| 일기·꿈 태그 탭 | `searchEntries({ tagIds, type })` → 하단 결과 목록 → EntryDetail |
| 일자 태그 탭 | `getJournalDayTagYears` + 연도 ‹ › → `searchJournalDaysByTag` → 오늘 탭 `{ date }` |

**API**
- `GET /api/journal/day/tags?yy=&mnth=`
- `GET /api/journal/entry/tags?yy=&mnth=&type=DIARY|DREAM`
- `GET /api/journal/day/tag/{tagId}/years` (일자 태그 연도 목록)
- `GET /api/journal/days?viewType=SEARCH&tagId=&yy=` (일자 태그 결과)

---

### 10. SearchScreen / 검색 (`src/screens/SearchScreen.tsx`)

**상태**: 구현 완료

**목적**: 키워드로 저널 엔트리 전체 검색 (꿈 / 일기 / 전체 필터 지원)

| 요소 | 설명 |
|---|---|
| 헤더 | "DreamDiary" kicker + "검색" 타이틀 |
| 검색 입력 | TextInput + "검색" 버튼 / 키보드 returnKeyType="search" 지원 |
| 타입 필터 | 전체 / 꿈 / 일기 칩 — 선택 즉시 재검색 |
| 결과 카드 | 타입 뱃지 + 날짜(`stdrdDt`) + 제목 + 본문 미리보기 140자 |
| 결과 카드 탭 | `navigation.navigate("EntryDetail", { entry, isDream })` |
| 빈 결과 | "검색 결과가 없습니다." |
| 오류 상태 | 빨간 오류 텍스트. 서버 `message`를 우선 표시하고 검색 실패를 빈 결과로 오인하지 않도록 직전 성공 결과를 유지 |
| 로딩 상태 | 검색 버튼 내 ActivityIndicator |

**API** (`GET /api/journal/entries`)
- `searchKeywords[0]=KEYWORD` — 키워드 (List<String> 인덱스 바인딩)
- `type=DREAM|DIARY` — 타입 필터 (생략 시 전체)
- 검색 조건 없으면 백엔드가 `IllegalArgumentException` → 빈 문자열 호출 금지

**UX 특이사항**
- `KeyboardAvoidingView` 래핑 (iOS: `padding`, Android: `undefined`) — 검색 입력 시 키보드가 인풋을 가리지 않게 처리

**내비게이션**
- 하단 탭 3번째 "검색" 탭 (`🔍` 이모지 아이콘)
- 검색 결과 카드 탭 → `EntryDetail` 스택 push


### 11. InterpretationScreen / 꿈 해석 (`src/screens/InterpretationScreen.tsx`)

**상태**: 구현 완료

**목적**: 꿈 엔트리에 대한 해석(메모) 목록 조회·추가·삭제

| 요소 | 설명 |
|---|---|
| 헤더 | `‹ 뒤로` + "꿈 해석" 타이틀 |
| 꿈 미리보기 | 원본 꿈 본문 최대 120자 (보라색 테마 카드) |
| 해석 목록 | 해석 텍스트 카드 + 삭제 버튼 (Alert 확인) |
| 빈 상태 | "아직 해석이 없습니다." |
| 새 해석 입력 | multiline TextInput, minHeight 120 |
| 추가 버튼 | `POST /api/journal/interpretations` (multipart/form-data) |
| 삭제 | `DELETE /api/journal/interpretation/{id}` → 목록에서 즉시 제거 |

**API**
- `GET /api/journal/interpretations?refId={entryId}&refContentType=JOURNAL_DREAM` — 목록 조회
- `POST /api/journal/interpretations` (form: `refId`, `refContentType=JOURNAL_DREAM`, `content`) — 추가
- `DELETE /api/journal/interpretation/{id}` — 삭제

**내비게이션**
- params: `{ entry: JournalEntry }`
- EntryDetailScreen(꿈) 본문 하단 "🌙 꿈 해석 보기 →" 버튼 → push
- 뒤로 버튼 → goBack()

---

### 12. AIChatScreen / AI 대화 (`src/screens/AIChatScreen.tsx`)

**상태**: 구현 완료 (REST + STOMP WebSocket)

**목적**: AI(Ollama LLM)와의 채팅 인터페이스. 세션별 대화 이력 조회 및 실시간 대화.

| 요소 | 설명 |
|---|---|
| 헤더 | `‹ 뒤로` 버튼 + 세션 제목(중앙) + `+ 새 대화` 버튼 |
| 세션 탭 | 세션 2개 이상 시 수평 스크롤 탭 표시 — 탭하면 해당 세션 메시지 로드 |
| 세션 없음 | "첫 대화 시작하기" 버튼 → `POST /chat/sessions` |
| 세션 탭 롱프레스 | Alert 확인 후 `DELETE /chat/sessions/{id}` |
| 메시지 목록 | USER(우측 갈색 버블) / ASSISTANT(좌측 보라 버블) 구분 표시 |
| AI 아바타 | 28×28 보라 원형 `AI` 레이블 |
| 입력·전송 | multiline TextInput + 「전송」 / 응답 대기 시 「중단」 |

- 세션·메시지 목록 조회 실패는 별도 `sessionsError`/`messagesError` 상태로 서버 `message`를 표시하고 `다시 시도` 버튼을 제공한다. 메시지 재조회 실패는 현재 세션과 직전 성공 메시지를 유지하며, 세션 목록 오류가 아닌 메시지 영역 오류로 표시한다.

**API** (REST)
- `GET /chat/sessions` — 내 세션 목록
- `POST /chat/sessions` (JSON) — 새 세션 생성
- `DELETE /chat/sessions/{id}` — 세션 삭제
- `GET /chat/sessions/{id}/messages` — 세션 메시지 조회

**내비게이션**
- TodayScreen QuickCapturePanel AI 모드 · ProfileScreen 「AI 대화」 → `rootNav.navigate("AiChat")`
- RootStack 스크린 (`AiChat: undefined`)

**WebSocket (STOMP 1.2)** — `src/api/chatStomp.ts`, `src/hooks/useChatStomp.ts`
- 연결: `ws(s)://{API_HOST}/chat` — 핸드셰이크 `Authorization: Bearer {accessToken}` (`src/auth/accessToken.ts`, login/refresh 응답 헤더에서 저장). 쿠키 `jwt` 폴백은 서버 `JwtTokenProvider.resolveToken(ServerHttpRequest)` 지원
- CONNECT 후 구독: `/topic/chat/session/{sessionId}`, `/user/queue/session-invalid` (현재 로그인 사용자 전용 세션 만료 알림)
- 전송: `/app/chat/session/{sessionId}/send` (text/plain 본문)
- 취소: `/app/chat/session/{sessionId}/cancel`
- MESSAGE 수신 → `rsltObj` ChatMessage append (웹 `chat.ts` 와 동일 프레이밍)

---

## IA 계약 (Daily-first)

> 웹(Vue) 저널은 월간/주간/태그클라우드·어사이드가 한 화면에 공존한다.
> 모바일은 **실행(Daily)** 과 **탐색(Calendar / Tag / Search)** 을 분리하는 것이 기본 철학이다.
> 관리자·연간결산·스레드 등 관리·분석 화면은 웹이 제공한다.

### UI 철학 (웹 vs 모바일)

| 축 | 웹 | 모바일 |
|---|---|---|
| 기본 단위 | 주간/월간 목록 + 어사이드 필터 | **하루(DAILY)** |
| 태그클라우드 | 목록 상단 상시 노출 가능 | **태그 탭** |
| 입력 | 툴바·모달·다중 뷰 전환 | FAB / 하단 시트 / 단일 포커스 |
| 탐색 | 같은 레이아웃 안 필터 | 달력·태그·검색 탭으로 분리 |

### 하단 탭

`MainTabs`는 `오늘` · `달력` · `태그` · `검색` · `나`의 5탭이며 `Today`가 초기 탭이다.

- **실행 축**: `TodayScreen`이 선택 날짜의 Daily 허브다. 오늘에는 `QuickCapturePanel`을 표시하고 모든 날짜에 해당 날짜 기준 `AddEntry` FAB를 제공한다.
- **탐색 축**: `CalendarScreen`은 날짜, `TagExploreScreen`은 월간 태그, `SearchScreen`은 키워드·유형으로 기록을 찾는다.
- **일자 단일 경로**: 달력·일자 태그 결과는 `navigateToDailyHub`를 통해 `Today` 탭에 `{ date }`를 전달한다. 일자 목록은 `JournalDayList`와 `useJournalDay`가 담당한다.
- **상세·쓰기 표면**: `AddEntry`·`EntryDetail`·`EntryEdit`·`InterpretationDetail`·`AiChat`은 RootStack에서 push한다.
- **계정 축**: `ProfileScreen`은 계정 정보·월간 통계·AI 대화 진입·로그아웃을 제공한다.
- **웹과의 경계**: 모바일은 Daily 실행과 달력·태그·검색 탐색을 제공하고, 관리·연간 결산·주간·월간 분석 화면은 웹이 제공한다.
