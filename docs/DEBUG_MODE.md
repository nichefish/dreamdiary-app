# Frontend Debug Mode

## 개요

개발·디버깅 시 저널 화면의 접힘(collapse) 상태·라이프사이클·신호 전파를 시각적으로 확인할 수 있는 인라인 디버그 오버레이.

프로덕션 코드에 포함되지만 기본 비활성이며, `localStorage` 플래그로 켜고 끈다.

## 켜기 / 끄기

브라우저 콘솔에서:

```js
// 켜기
localStorage.setItem("debug_collapse", "true");
location.reload();

// 끄기
localStorage.removeItem("debug_collapse");
location.reload();
```

새로고침 없이도 Vue 반응성으로 즉시 반영되지만, computed가 localStorage를 매 렌더마다 읽으므로 reload가 확실하다.

## 표시 내용

### JournalEntryItem (일기/꿈/노트)

빨간 작은 글씨로 본문 위에 표시:

```
[E#1234] isCollapsed=false | lc=PENDING | force=null | localOvr=null | signal=expand
```

| 필드 | 의미 |
|------|------|
| `E#` | 엔트리 ID |
| `isCollapsed` | 최종 접힘 상태 |
| `lc` | lifecycle key (OPEN/PENDING/RESOLVED) |
| `force` | 상위 챕터에서 전달된 `forceCollapsed` prop |
| `localOvr` | 사용자가 직접 토글한 로컬 오버라이드 (null/true/false) |
| `signal` | 하위 리플렉션에 전파하는 신호 (expand/collapse/null) |

### JournalReflectionItem (리플렉션 임베드)

빨간 작은 글씨로 본문 위에 표시:

```
[R#5678] isCollapsed=true | lcKey=PENDING | signal=null | localOvr=null
```

| 필드 | 의미 |
|------|------|
| `R#` | 리플렉션 ID |
| `isCollapsed` | 최종 접힘 상태 |
| `lcKey` | lifecycle key |
| `signal` | 부모 엔트리에서 받은 `forceCollapsedSignal` prop (expand/collapse/null) |
| `localOvr` | 사용자가 직접 토글한 로컬 오버라이드 |

## 접힘 우선순위 (참고)

### 엔트리

1. `localCollapsedOverride` (사용자 직접 토글)
2. `forceCollapsed` (챕터 전파)
3. lifecycle 자동 접힘 (PENDING/RESOLVED, `disableLifecycleCollapse`로 억제 가능)
4. 서버 COLLAPSED 상태

### 리플렉션

1. `localCollapsedOverride` (사용자 직접 토글)
2. `forceCollapsedSignal` ("expand"/"collapse")
3. lifecycle 자동 접힘 (PENDING/RESOLVED)
4. 서버 COLLAPSED 상태

## 확장

다른 디버그 플래그를 추가할 때도 같은 패턴을 사용한다:
- `localStorage.setItem("debug_<feature>", "true")`
- 컴포넌트에서 `computed(() => localStorage.getItem("debug_<feature>") === "true")`
- 조건부 `v-if="debugXxx"` 블록으로 메타 정보 표시
