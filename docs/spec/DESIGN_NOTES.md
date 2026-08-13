# DESIGN NOTES

문서를 새로 쓸 때는 **무엇을 남길지**를 `docs/DEV_NOTES.md` 의 「문서 기록 원칙」을 본다.

## 저널(journal)
- journal-day
  - journal-chapter
    - journal-diary
    - journal-dream
    - journal-note
    - journal-reflection
- journal-thread
- journal-annual
  - journal-annual-review

---

### 저널 일자(journal-day)
- **축별 완결·쓰기 잠금** (diaryResolvedYn / dreamResolvedYn): 일자 provide 가 있는 목록·상세뿐 아니라 검색 팝업·엔트리 뷰 모달도 잠겠야 한다. 엔트리 DTO에 일자 플래그를 투영하고 mergeDayResolvedAxis 로 provide·DTO 를 병합한다. 서버 JournalDayResolvedGuard 가 우회 POST 를 거절한다.
- 클라이언트 쪽 마이그레이션·Phase 요약표는 `docs/DEV_NOTES.md`(저널 일자 마이그레이션 롤업)를 본다.
- 사용자별 일자 데이터.
- 저널 일자 신규 등록 후에는 `JournalDayBootstrapService`가 시스템 요약 챕터(`summaryYn=Y`)와 빈 DIARY 엔트리 구조를 보장한다. 이미 DIARY 챕터가 있으면 추가 생성하지 않는다.
- 월간(monthly)/달력(calendar) 조회: 정형 데이터. 성능 최적화 및 응답 일관성 확보를 위해 캐시 기반 조회 유지.
  - "월 단위 데이터는 해당 월 이후부턴 조회 빈도 대비 변경 빈도가 낮다. DB 조회 비용보다 캐시 유지 + 서버 필터링이 더 효율적이다."
  - 특정 년월(yyyy-mm) 단위로 데이터를 캐싱.
  - 조회 시 DB 재조회 없이 캐시 데이터 사용. 필터링은 DB가 아닌 서버(Java) 레벨에서 수행. 동일한 캐시 데이터를 기반으로 다양한 필터 조건 대응.
  - 등록/수정/삭제 발생 시 → 해당 년월 캐시 전체 무효화(invalidate).
  - 인덱스 처리 위해 journal_date에서 파생된 yy, mnth 컬럼 유지
- 검색 (search) 조회: 비정형 데이터. 월간 조회와 분리하여 DB 기반 조회 전략을 유지한다.
  - "검색은 범위가 비정형적이며(기간, 키워드 등) 캐시 효율이 낮고 관리 비용이 높다. 따라서: 캐시 적중률보다 정확성과 유연성을 우선한다."
  - 캐시를 사용하지 않거나, 매우 제한적으로만 사용. 모든 조건은 DB 쿼리로 직접 처리.

### 저널 챕터(journal-chapter)
- 저널 일기를 담는 묶음.
  - "단순 컨테이너가 아니라, 일기들의 상위 서사 프레임에 더 가깝다. '구조적 구획'이면서 동시에 '의미 있는 묶음'이다."
  - "chapter는 삭제하면 안 된다. 오히려 앞으로 더 중요해질 테이블이다."
  - "실제로 중요한 것은 데이터 값이 아니라 도메인 경계다. chapter는 이미 '하루의 사건 단위'라는 도메인 경계를 가지고 있다."
- 최초 non-DREAM 챕터는 제출 Prefix와 무관하게 시스템 요약 역할(`summaryYn=Y`)로 지정한다. 이 역할은 사용자가 선택하는 Prefix가 아니며 일반 챕터에서 직접 지정할 수 없다.
- 요약 챕터는 헤더에 고정 `요약` 명칭을 표시하고 Prefix를 보유하지 않는다.
- 일반 챕터 헤더는 선택한 개인 Prefix 이름을 타입 라벨 뒤에 한 줄로 붙인다.
  - 예: `일기:관계`, `노트:리뷰`
  - 타입 라벨 바로 뒤에 공백 없이 `:`를 붙인다.
  - Prefix 이름은 헤더 크기(`fs-6`)를 상속하고 설정 색상(없으면 `#287D94`)을 쓴다.
  - 시스템 요약 챕터에는 Prefix 대신 고정 `요약` 명칭만 표시한다.
- 글접기시 하위 일기 태그 묶음을 요약해서 보여준다.

### 저널 일기(journal-diary)

### 검색
- 새 창
- 검색: Ajax 대신 URL 기반 접근 처리
  - "검색은 상태다. 검색 결과는 재현 가능해야 하고 주소 기반 공유가 가능해야 한다."
- 중복 키워드, 중복 태그 검색 처리

### 저널 꿈(journal-dream)

- 저널 꿈 태그 해석 추가
  - "태그에 해석을 붙이면 해석의 일관성이 생긴다. 상징 데이터가 누적된다. 반복 상징의 변주를 추적할 수 있다."
  - "dreamdiary가“상징-사례-변형” 구조로 진화한다. 즉, 꿈을 모으는 시스템 → 상징 체계를 구축하는 시스템으로 격상된다."
  - "이건 해석을 버리는 게 아니라 해석의 기준면을 하나 더 두는 것이다."

### 저널 노트(journal-note)

- NOTE 는 **chapter 타입**(`ChapterType.NOTE`)으로 존재한다. 현재 쓰기 경로에서 NOTE chapter 의 entry 는 `JOURNAL_DIARY` 로 저장된다(`JOURNAL_NOTE` contentType 은 예약).
- Reflection 흡수 후 NOTE 의 추가 역할: day 없는 **orphan-NOTE 버킷 chapter** — 무소속 사유·이관 시 live target 없는 행의 착지처. 정본: `docs/migration/journal/reflection-absorption.md` §4.3.

### 저널 리플렉션(journal-reflection)

- **Reflection**(`JOURNAL_REFLECTION`)은 `journal_reflection` 별도 Aggregate다. `refId`/`refContentType` 대상은 필수이며 챕터·정렬·태그를 소유하지 않는다.
- 등록·수정·삭제·상세는 `/api/journal/reflection(s)` 전용 API와 `JournalReflectionService`가 담당한다.
- 표시는 대상 엔트리 아래 `JournalReflectionItem` 임베드로 제공하며, 대상 엔트리 접힘 상태와 일자 aside 기본 접힘 모드를 따른다.
- 태그클라우드·결산·챕터 접힘 요약과 검색 태그·state는 DIARY/DREAM 요청 타입 단일 축을 사용한다. Reflection 저장은 태그 캐시를 무효화하지 않는다.
- Reflection은 스레드 소속 대상이 아니며, 대상 본문과 함께 읽히는 하위 사유로 유지한다.
- 대상·라이프사이클·검색 계약 정본: `docs/spec/REFLECTION_ONE_TYPE.md`.
- as-built: `docs/migration/journal/{screen,interaction,component}-spec.md` 의 Reflection 항목.

### 저널 스레드(journal-thread)

- **도메인 한 줄 정의**  
  **시간축 위에서 여러 journal entry(일기·꿈·노트·해석 등)를 관통하는 상위 서사 단위**다.  
  「관련글 묶음」이나 단순 카테고리가 아니라, *시간에 따라 변형·누적되는 의미 구조*를 보존하기 위한 aggregate에 가깝다.

- **용어·코드베이스 정렬**  
  저장소·API·`ContentType`에서는 **`journal_thread` / `JOURNAL_THREAD`** 로 수렴했다.  
  의미론적으로는 `arc`, `narrative`, `series`, `constellation`, `cycle` 계열과 같은 층위와 맞닿아 있고, 과거 `journal_sbjct`(subject) 같은 이름은 「주제/제목」 연상에 가까워 **이 층위를 담기에는 약했다**고 본다. 이름을 바꾼 것은 기능 실패 선언이 아니라 **아직 제대로 언어화하지 못했던 정체성을 코드와 맞추려는 정렬**에 가깝다.

- **태그·엔트리와의 구분**  
  - **Journal entry**는 대개 **특정 시점의 기록(순간)** 이다.  
  - **저널 스레드(의미 목표: arc)**는 여러 순간을 이어 **동일 흐름으로 읽히게 만드는 시간적·해석적 연속성**을 담는다.  
  - **태그**는 주로 분류·속성·검색 보조 축이다. 스레드가 담는 것(정서적 연속, 해석의 누적, 서사의 진화)과 역할이 다르다.

- **스레드 소속과 엔트리 관계의 경계**
  - 저널 스레드는 특정 일자의 엔트리로 쓰기 어려운 상위 서사를 제목·본문으로 직접 서술하는 독립 aggregate다. 엔트리는 이 상위 서사에 **소속**되고, 스레드 상세는 소속 엔트리를 모아 보여주는 방향을 갖는다.
  - 엔트리끼리 수동으로 연결하여 하나의 전개 흐름을 복원하는 기능은 스레드 소속이 아니다. 별도 상위 글을 만들지 않고 `related_content`의 `FLOW` 관계를 따라 구성되는 엔트리 그래프다.
  - 두 기능은 타임라인 형태로 보일 수 있지만 대체 관계가 아니다. 스레드의 중심은 독립적인 상위 서사이고, FLOW의 중심은 사용자가 출발점으로 선택한 엔트리다.

- **결정 변경 (0.24, 스레드 소속 구현) — FLOW 를 스레드 소속으로 수렴**
  - **변경 전**: 위 항목대로 스레드 소속과 FLOW 를 «대체 관계가 아닌» 별개 축으로 유지한다고 봤다.
  - **변경 후**: 두 축을 유지하지 않고 **스레드 소속 하나로 수렴**한다. FLOW 는 제거 대상이다.
  - 근거:
    - FLOW 는 컨테이너가 없어 흐름의 정체성이 없다. 매 요청마다 연결 컴포넌트를 순회해 흐름을 «계산»해야 하고, 이름을 붙일 자리도 없다.
    - 간선 모델은 **한 엔트리가 두 흐름에 속하는 경우를 표현하지 못한다**. 두 흐름이 한 엔트리를 공유하면 컴포넌트가 하나로 합쳐져 버린다.
    - 반대로 스레드는 컨테이너만 있고 멤버가 없었다. 소속 테이블이 생기면 이 결손이 사라지고, 위 두 문제도 같이 해소된다.
    - 실제 사용 데이터에서 FLOW 간선 5건이 모두 특정 엔트리 하나를 향한 성형(star) 구조였다. 사용자는 이미 «간선»이 아니라 «소속»으로 쓰고 있었다.
  - 단계:
    1. **완료** — 소속 테이블(`journal_thread_entry`) + 백엔드 API (`V0.24.7`)
    2. **완료** — FLOW 간선 → 소속 이관 (`V0.24.8`). 간선 29건 / 엔트리 33개 / 연결 컴포넌트 4개를 스레드 4건 + 소속 33건으로 옮겼다. 네 컴포넌트 모두 성형(star) 구조였다(허브 차수 합 26/29) — 사용자가 이미 «간선»이 아니라 «소속»으로 쓰고 있었다는 근거다. `related_content` 의 FLOW 29행은 이관 직후 롤백 여지를 위해 잠시 남겼다가 4단계에서 소프트 삭제했다.
    3. **완료** — ⋯ 메뉴의 FLOW 항목을 소속 UI 로 교체 (나-2a·나-2b·나-2c). 「스레드에 추가」 hover 서브메뉴(소속 토글 + 「새 스레드로 시작」)와 본문 소속 스레드 칩. 이후 전용 후보 API와 제목 검색·분류 필터, 엔트리별 재조회까지 완료했다.
    4. **완료** — `related_content` 의 FLOW 관련 데이터·코드 제거 (다-2). 다-2a: FLOW 29행 소프트 삭제(V0.24.11). 다-2b: 백엔드 FLOW 모델(FlowDto/FlowEntryDto/FlowSummaryDto)·서비스(RelatedContentFlowService)·컨트롤러 엔드포인트·`RELATED_FLOW`·`RelationType.FLOW` enum·`flowSummary` 주입 제거. 다-2c: 프론트 `JournalFlowSummary`·`normalRelatedList` FLOW 필터·`relationTypeLabel` flow·`enum.relation-type.flow` 제거. 

  - **운영 DB 이관 기록 정책**: 위 `V0.24.7`·`V0.24.8`·`V0.24.11`은 운영 DB에 직접 반영한 일회성 작업 식별자다. 반영 확인 후 해당 SQL 파일을 저장소에서 의도적으로 제거했으므로, 현재 migration 디렉터리에 파일이 없는 상태를 구현 누락이나 복구 TODO로 취급하지 않는다. 새 환경의 최종 구조 SSOT는 `schema/full/mariadb/schema-journal-mariadb.sql`의 `journal_thread_entry` 정의다.

**수렴 완료** — FLOW 축은 데이터·백엔드·프론트에서 모두 사라졌고, 흐름은 저널 스레드 소속(journal_thread_entry) 단일 축으로 수렴했다.
  - 이관 스레드 제목은 허브 엔트리 ID 를 쓴 중립 식별자(`흐름 #2005`)다. 저널 본문에서 딴 제목은 저장소에 개인 기록을 남기므로 쓰지 않는다. 사용자가 화면에서 직접 바꾸는 것을 전제로 한다.
  - 3단계가 끝나기 전까지 두 경로가 잠시 공존하지만 이는 수렴 과정의 중간 상태이지 유지 대상이 아니다.

- **스레드-엔트리 소속 계약 (`journal_thread_entry`)**
  - 스레드 1 : 엔트리 N 이 아니라 **N:M** 이다. 한 엔트리가 여러 스레드에 속할 수 있다 — FLOW 가 표현하지 못하던 지점이고, 이 구조를 택한 주된 이유다.
  - 등록은 소속 행 조회·복원·INSERT 전에 스레드와 대상 엔트리의 존재·현재 사용자 소유권을 모두 검증한다. 존재하지 않는 엔트리는 not found, 타인 소유 엔트리는 access denied로 거부하며 소속 쓰기를 수행하지 않는다.
  - 소속 후보는 일반 목록 첫 페이지를 잘라 쓰지 않고 전용 `GET /api/journal/threads/candidates` 계약으로 조회한다. 본인 스레드만 대상으로 현재 엔트리 소속 여부 → 최근 활성 소속 추가 시각 → 활성 소속 수 → 스레드 수정·생성 시각 순으로 정렬하고, 제목 검색·분류 필터·조회 상한을 서버에서 적용한다. 현재 스키마에는 별도 `last_used_at`이 없으므로 「최근 사용」은 가장 최근 활성 소속의 `created_at`으로 정의한다.
  - 소속 해제는 **소프트 삭제**(`deleted_at`)다. UNIQUE KEY `(thread_id, entry_id, created_by)` 가 `deleted_at` 을 포함하지 않으므로, 해제한 소속을 다시 등록하면 INSERT 가 제약에 걸린다. 등록 경로는 소프트 삭제된 행을 먼저 찾아 **되살린다**(`findAnyByPair` → `reviveById`). 새 행을 만들지 않는다.
  - 등록·해제 모두 **멱등**이다. 이미 소속이면 변경 없이 성공, 이미 해제면 변경 없이 성공으로 응답한다.
  - `created_by` 를 UNIQUE KEY 에 포함해 **본인 소유 범위 안에서만** 묶인다 (`tag_content`·`related_content` 와 동일한 관례).
  - 스레드 소속의 `journal_thread_entry.sort_order` 는 스키마에 nullable 로 남아 있지만 **현재 표시 순서 계약에는 사용하지 않는다**. 스레드 상세의 선후는 엔트리 일자 오름차순, 동일 일자는 원본 엔트리 `journal_entry.sort_order` 오름차순으로 결정하고, 값이 없거나 중복된 경우에만 엔트리 ID를 tiebreak로 쓴다. 구체적인 사용 사례 없이 별도 사용자 지정 순서 UI/API를 연결하지 않는다.
  - 소속 조회 DTO 는 엔트리 **본문·제목을 담지 않는다**. 목적은 «무엇이 묶여 있는가»이고, 표시에 필요한 엔트리 상세는 기존 엔트리 조회 API 로 가져간다.
  - 기존 스레드 52건의 제목·본문·태그·분류는 **일절 변경하지 않는다**. 이 단계는 테이블 추가와 API 뿐이다.

- **“거대한 애매한 폴더” 리스크와 현재 경계**
  스레드에는 자기 설명력이 필요하지만, 이를 별도 메타 필드의 수로 해결하지 않는다.
  - 제목과 본문이 스레드의 정체성과 상위 서사를 직접 설명하는 현재 SSOT다.
  - 시작·최근 시점이 필요하면 소속 엔트리의 최초·최종 일자에서 파생한다. 별도 시작·종결 시점을 저장하지 않는다.
  - 스레드는 사람·관계·반복 주제처럼 끝나지 않을 수 있으므로 라이프사이클, 진행/종결 상태와 종결 시점을 요구하지 않는다. 상태에 연결되는 보관·필터·재개 동작이 구체화되기 전에는 추가하지 않는다.
  - 대표 엔트리(앵커)는 두지 않는다. 스레드의 중심은 특정 엔트리가 아니라 여러 시점의 기록을 함께 읽는 상위 서사이며, 앵커는 연결 해제·삭제 시 불필요한 정합성 계약을 만든다.
  - 핵심 질문은 현재 본문으로 표현한다. 별도 검색·필터·화면 강조·분석 입력처럼 구조화된 소비처가 생길 때만 독립 필드를 재검토한다.

- **스레드 내 위치·역할의 현재 결정**
  - 소속은 한 엔트리가 여러 스레드에 속할 수 있는 N:M 구조를 유지한다.
  - 읽기 순서는 엔트리 일자와 원본 엔트리 인덱스에서 자연스럽게 파생한다. 현재 계약은 일자 → 원본 엔트리 `sort_order` → ID 오름차순이며 별도 수동 순서를 제공하지 않는다.
  - 사건·꿈·해석 같은 기본 구분은 엔트리 `contentType`에 이미 있다. 별도 소속 역할은 그 값을 소비하는 구체적인 화면·검색·분석 요구가 생기기 전에는 추가하지 않는다.
  - 따라서 nullable `sort_order`와 별도 `role`은 현재 제품 backlog가 아니다. 시간순 읽기가 실제 사용 사례를 충족하지 못한다는 근거가 생기면 그때 계약부터 다시 정한다.

- **현재 구현과의 관계**
  제목·본문과 N:M 소속, 시간순 상세 보기가 현재 제품·도메인 계약이다. 위에서 채택하지 않은 메타는 구현이 뒤처진 것이 아니며 수렴 작업 backlog로 취급하지 않는다.

- **저널 스레드 구현 TODO**
  - [x] **소속 대상 엔트리 소유권 검증**: `JournalThreadEntryService.regist`가 스레드 소유권뿐 아니라 대상 `entryId`의 존재와 현재 사용자 소유권도 소속 조회·복원·저장 전에 검증한다. 직접 API 요청으로 타인·미존재 엔트리 소속 행이 생기지 않으며, 실패 경로는 소속 쓰기를 수행하지 않는다.
  - [x] **소속 후보 탐색**: 「스레드에 추가」 서브메뉴의 최근 스레드 7개 임시 노출을 전용 후보 계약으로 교체한다.
    - [x] 백엔드: 현재 소속 우선, 최근 활성 소속 추가 시각, 활성 소속 수, 스레드 수정·생성 시각 랭킹과 제목 검색·분류 필터·조회 상한을 제공한다.
    - [x] 프론트엔드: 서브메뉴를 후보 API로 전환하고 검색·분류 입력, 엔트리별 재조회, 요청 경합과 소속 변경 후 갱신을 처리한다.
  - [x] **상세 엔트리 액션 호스트**: 스레드 상세의 소속 엔트리는 읽기 전용 복제본이 아니라 원본 엔트리다. 저널 일자와 같은 액션을 유지하고 필요한 자식 모달을 스레드 레이아웃에 마운트하며, 액션 성공 후 상세 본문·집계 태그·소속 엔트리를 함께 재조회한다.
  - [x] **상위 서사 메타 타당성 재검토**: 제목·본문이 자기 설명의 SSOT다. 시작·최근 시점은 소속 엔트리에서 파생하며, 라이프사이클·진행/종결 상태·종결 시점·대표 엔트리·별도 핵심 질문 필드는 현재 요구하지 않는 것으로 확정했다.
  - [x] **스레드 내 위치·역할 타당성 재검토**: 날짜순 읽기를 현재 계약으로 확정했다. nullable `sort_order`는 표시 순서에 사용하지 않고, 별도 역할은 `contentType`으로 충족되지 않는 구체적인 소비처가 생길 때만 재검토한다.

### 저널 결산(journal-annual)
- 결산 주기: 연간으로 고정.
  - "월간 결산은 지금 기준으로 투머치다."
  - "결산의 베스트 프랙티스는: 시간이 충분히 지나 사건이 ‘정리되고’, 의미가 ‘침전된 뒤’에 하는 해석이다."
  - "월 단위는: 아직 사건이 닫히지 않았고, 감정이 진행 중이고, 반복/전환 여부가 보이지 않는다."
  - "월별 결산은 결국: 일기 요약, 감정 로그, “이번 달 뭐 했지?” 정리가 된다. 이건 결산이 아니라 로그 압축이다."
- URL: KEY 기반 접근 대신 년도 기반 접근
  - "yy UNIQUE가 이미 걸려 있다면, 년도 기반 url은 ‘문제 없음’이 아니라 ‘의도에 정확히 부합’한다."

## 로그
- 로그 기준 재설정 중.
  - "페이지 조회도, 데이터 조회 API도, 운영 관점에서 볼 때 예외 없으면 성공이다." “HTTP status 기준이 충분하다."
- 모든 요청에 traceID 추가.
  - "Filter가 HandlerInterceptor보다 먼저 작동한다." "Filter → Interceptor → Controller. 응답 시에는 역순으로 빠져나온다."
  - "Filter에서 모든 요청에 traceId를 생성한다."
  - "이 구조면 서버 로그 ↔ DB access 로그 ↔ audit 로그, 전부 하나의 traceId로 묶인다. 이게 운영 관측성(Observability)의 최소 단위다."

## 말머리(Prefix)

> PERSONAL/GLOBAL Scope와 게시판 boardKey 수렴의 상세 SSOT는 `docs/spec/PREFIX_SCOPE_DESIGN.md`다.

### 도메인 계약

- **용어와 역할**
  - 사용자 UI 명칭은 **말머리**, 코드·도메인 명칭은 **Prefix**로 통일한다.
  - Prefix는 제목 앞에 표시되어 콘텐츠를 읽는 대표 맥락을 제공하는, 게시판의 보편적인 말머리다. 콘텐츠를 여러 축으로 분류하는 Category/Taxonomy가 아니다.
  - 태그는 자유 생성·다중 선택되는 내용 키워드다. Prefix는 사용자가 미리 관리하는 평면 선택지 중 콘텐츠당 하나만 고르는 대표 슬롯이며 두 기능을 합치지 않는다.
  - 담당자·상태·프로젝트처럼 서로 다른 축이 추가로 필요해져도 Prefix를 다중화하지 않는다. 기존 태그로 표현하거나 실제 요구가 확인된 축을 별도 속성으로 설계한다.
  - 개인 저널과 게시판의 말머리는 관리 권한과 적용 대상은 다르지만, 「특정 범위가 관리하는 평면 선택지 중 콘텐츠당 하나를 선택한다」는 같은 도메인 계약을 가진다. 소비처마다 `JournalPrefix`, `BoardPrefix` 테이블을 만들지 않고 공통 Prefix 기반축을 사용한다.
  - **PrefixScope**는 Prefix 목록의 소유·선택 경계다. 개인 목록은 `(PERSONAL, user_id, content_type)`, 공용 목록은 `(GLOBAL, content_type)`으로 정규화하며 한 관리 문맥의 평면 목록 하나를 묶는다.

- **카디널리티와 범용 범위**
  - 콘텐츠는 Prefix를 **0개 또는 1개** 가진다. 선택하지 않은 상태를 허용하며 같은 콘텐츠에 둘 이상의 Prefix를 연결하지 않는다.
  - 하나의 PrefixScope는 여러 Prefix를 가지며, 각 Prefix는 정확히 하나의 PrefixScope에 속한다. 관계는 `PrefixScope 1:N Prefix`, `Content 0..1 Prefix`로 고정한다.
  - 개인 Scope에서는 사용자가, 게시판 Scope에서는 게시판 관리자가 해당 Scope의 Prefix 이름·색상·정렬·활성 상태를 관리한다. 선택지는 계층·Scheme·Assignment 없이 Scope별 평면 목록이다.
  - PrefixScope에는 `SINGLE|MULTI` 같은 선택 방식이 없다. 콘텐츠당 `prefix_content` 단일 연결(0..1)이라는 소비자 계약이 카디널리티를 결정한다.
  - 범용성은 소비자 resolver가 관리 권한과 논리 content type으로 PERSONAL/GLOBAL PrefixScope를 확정하는 수준으로 제한한다. 새로운 소비처는 실제 관리 권한·선택 화면·Scope 일치 검증·삭제 계약이 함께 마련될 때만 추가한다.
  - 개인 저널과 게시판이 현재 소비 대상이다. PrefixScope 도입 자체가 임의의 `ContentType`이나 ATTACHABLE 전체에 Prefix를 허용하지 않는다.

- **데이터·쓰기 계약**
  - 목록과 선택 연결을 분리한다. 목록은 `prefix_scope`+`prefix.scope_id`가, 선택은 attachable 연결 `prefix_content`가 담당한다.
  - **목록 정체성은 `prefix_scope(scope_type, owner_key, content_type)`로 정규화한다.** PERSONAL은 실제 `user_id`, GLOBAL은 `user_id=NULL`과 생성 컬럼 `owner_key=0`을 사용한다. Prefix는 정확히 하나의 Scope에 속한다(`prefix.scope_id`).
  - Scope는 사전 프로비저닝하지 않는다. 개인 또는 게시판 관리 문맥에서 해당 content type의 첫 Prefix를 등록하는 시점에 lazy 생성한다.
  - 개인 저널과 게시글 선택은 attachable 연결 `prefix_content(ref_id, ref_content_type, prefix_id)`에 저장하고 `PrefixEmbed`로 조립한다. 게시글의 `ref_content_type`은 동적 boardKey다.
  - `scope_type`은 `PERSONAL|GLOBAL` 소유 유형만 표현하고 임의 테이블을 가리키는 `scope_ref_id` 다형 pseudo-FK는 쓰지 않는다. PERSONAL 소유자는 `prefix_scope.user_id` 직접 FK이며 GLOBAL은 사용자 소유자가 없다.
  - Prefix 소유권 SSOT는 `created_by`가 아니라 `prefix_scope.(scope_type, user_id, content_type)`와 `prefix.scope_id` 관계다. `created_by`·`updated_by`는 감사 정보로만 유지한다.
  - 선택 쓰기는 고른 Prefix가 콘텐츠 문맥의 Scope에 속하는지 서버가 최종 검증한다. 개인 저널은 `PrefixService.requireSelectable`, 게시글은 `PrefixService.requireSelectableGlobal`을 사용하며 게시글 Scope content type은 boardKey와 일치해야 한다.
  - 비활성 Prefix는 신규 선택지에서 제외하고 새 연결을 거부하되, 과거 콘텐츠의 표시와 다른 필드 수정 과정에서 기존 선택을 임의로 유실하지 않는다.
  - 콘텐츠 soft-delete 시 Prefix 선택지와 `prefix_content` 연결을 보존해 복원 시 기존 선택을 유지한다.
  - 핵심 분기와 예외는 Scope 접근 권한 거부, 콘텐츠–Prefix Scope 불일치, 비활성 Prefix 신규 선택에 구조화 로그를 남긴다.
  - 개인 목록은 PERSONAL, 게시판 목록은 `GLOBAL + boardKey` Scope를 사용하며 모든 소비자는 `prefix_content` 단일 선택 경로를 사용한다.

- **관리 API와 권한 경계**
  - 개인 관리 UI와 API는 `/my/prefixes`·`/api/my/prefixes`를 유지하며 로그인 사용자의 `(user, content_type)`별 개인 Scope만 다룬다. 관리 화면은 Scope 존재 여부와 무관하게 작은 `저널` 도메인 헤더 아래 `일기 챕터 / 노트 챕터 / 일기 / 꿈 / 노트 / 스레드` 고정 6행을 표시하고, 행을 누르면 각각 `JOURNAL_CHAPTER_DIARY / JOURNAL_CHAPTER_NOTE / JOURNAL_DIARY / JOURNAL_DREAM / JOURNAL_NOTE / JOURNAL_THREAD` 목록을 조회하는 관리 모달을 연다. 이후 다른 도메인은 동일한 화면 카탈로그 그룹으로 추가하며 Prefix 영속 구조를 바꾸지 않는다. 초기 화면에서는 목록 조회나 말머리 미리보기를 하지 않는다.
  - 개인 활성 선택지는 `contentType`별 공통 클라이언트 캐시를 사용한다. 관리 등록·수정·활성 변경이 성공하면 해당 타입만 무효화하고 다음 소비 화면 진입에서 서버 확정 목록을 다시 조회한다. 정상 빈 목록과 동시 요청은 캐시하되 실패는 재시도하고, 로그아웃·사용자 전환 및 타입별 무효화 전에 시작한 늦은 응답은 세대·버전 검사로 폐기한다.
  - 사용자는 관리 대상을 추가·삭제하지 않는다. Prefix가 없는 대상도 고정 행으로 표시하며, 해당 관리 모달에서 첫 Prefix를 등록할 때 Scope를 lazy 생성한다. 일기·꿈·노트를 하나의 `JOURNAL_ENTRY` 논리 목록으로 합치지 않는다.
  - 게시판 Prefix 관리는 게시판 관리 문맥의 별도 UI와 `/api/board/groups/{boardId}/prefixes` 권한 API에서 수행한다. 일반 게시판 사용자는 `/api/board/{boardKey}/prefixes` 읽기 API로 활성 선택지만 조회한다.
  - API와 권한 서비스는 소비 문맥별로 분리할 수 있지만 PrefixScope·Prefix 영속 모델과 공통 불변식 검증은 공유한다.
  - 게시판 관리자에게 개인 Prefix 관리 권한을 주거나, 개인 사용자가 게시판 Scope를 자기 설정에서 변경하게 하지 않는다.

- **구현 상태** (dev_0.25.0 기준)
  - [x] 개인 목록은 `prefix_scope(PERSONAL, user_id, content_type)`, 게시판 목록은 `prefix_scope(GLOBAL, boardKey)`를 사용한다.
  - [x] `/my/prefixes`는 6개 개인 저널 대상을 관리하고 각 content type의 첫 등록에서 Scope를 lazy 생성한다.
  - [x] 일기·노트 챕터는 각각 `JOURNAL_CHAPTER_DIARY`, `JOURNAL_CHAPTER_NOTE` Scope를 사용하며 attachable 정체성은 `JOURNAL_CHAPTER`로 유지한다.
  - [x] 일기·꿈·노트 엔트리와 스레드는 각 PERSONAL Scope의 Prefix를 0..1개 선택한다.
  - [x] 게시판별 GLOBAL Scope는 관리 화면과 게시글 등록·수정·검색에서 같은 boardKey 목록을 사용한다.
  - [x] 모든 선택은 `prefix_content`와 `PrefixEmbed`를 사용하며 Scope 일치를 서버가 검증한다.
  - [x] 비활성 Prefix는 기존 콘텐츠에서 표시하고 동일 선택을 유지할 수 있으며 신규 선택은 거부한다.
  - [x] `prefix_scope`, `prefix`, `prefix_content` full schema와 운영 DB가 같은 계약을 사용한다.

### 댓글(comment)

### 단락(sectn)

### 태그(tag)

#### 태그 프로필(tag-profile)
- 태그클라우드 색상 설정 추가.
  - "태그를 색으로 분리하면 인지적 분류 비용이 급감한다." "이건 단순 스타일이 아니라 주의(attention)와 해석 레이어를 분리하는 기능이다."
- 태그클라우드 크기 고정(`cloudSizeLock`): MAX면 `ts-9`, MIN이면 `ts-1`로 고정(AUTO는 빈도 산출). 색(`textClass`)과 직교. sized 태그클라우드만(엔트리 본문 태그줄 제외).

### 메타(meta)

### 상태(state)

### 조회자(viewer)

### 관련글(related)
- "기존 BaseAttachableKey(postNo + contentType) 체계 위에 명시적 관계 레이어를 하나 더 얹는다."
- 컬럼은 방향성을 드러내는 `src/dst`보다 중립적인 `left/right`를 사용한다.
- "A-B"와 "B-A"를 같은 관계로 보고, 물리적으로는 1행만 저장한다. 조회는 양방향으로 푼다. 자기 자신과의 관계는 금지한다.
- 저장 전에 항상 pair를 정규화한다. 정규화 후 앞쪽을 `left_*`, 뒤쪽을 `right_*`에 저장한다.
- 관련글 API·연결 생성의 지원 타입은 `JOURNAL_DIARY`·`JOURNAL_DREAM`이다. 엔트리 삭제 후처리의 관련글 정리 오버로드는 미지원 타입(`JOURNAL_REFLECTION` 등)을 거절하지 않고 no-op 한다.

#### FLOW — 폐기된 결정 기록

- **현재 계약이 아니다.** 이전에는 `FLOW`를 두 엔트리가 같은 전개 흐름에 있다는 무방향 관계로 두고, 관계 컴포넌트를 전이 탐색해 날짜순으로 읽는 별도 축을 검토·구현했다.
- 0.24에서 이 모델은 위 「저널 스레드」 절의 결정 변경에 따라 폐기됐다. FLOW 데이터·API·DTO·enum·프론트 진입점은 제거됐고, 활성 구현과 향후 작업은 `journal_thread_entry` 소속 단일 축만 사용한다.
- 따라서 과거 FLOW 전이 탐색·병합 안내·지원 콘텐츠 확대·정렬 정책에 관한 체크리스트는 모두 **취소**됐으며 현재 backlog가 아니다. 당시 정렬·방향성 논의는 결정 이력으로만 보존한다.
- `CAUSE`처럼 방향을 암시하는 일반 관련글 타입의 의미 문제는 FLOW와 별개다. 무방향 `left/right` 저장 모델을 유지한다면 사용자 의미를 「인과 관계가 있음」 같은 대칭 라벨로 제한해야 한다.

## 사용자
최소 가입. 가입 후 수정/보강.

## 관리자 기능

### 캐시
- "캐시 무효화는 DB 상태와 강하게 결합되어 있으므로, 트랜잭션이 롤백될 가능성이 있는 상태에서 선행 실행되면 "캐시는 비워졌지만 DB는 롤백된" 불일치 상태가 발생할 수 있다. 따라서 실제 커밋이 완료된 이후에만 캐시를 무효화한다."

### Board / User 도메인 네이밍 (패키지·테이블)

(2026-04-18 결정 요약.)

- 테이블·도메인 언어로 `board`, `user` 를 유지하고, 패키지 충돌은 **구조 쪽에서 흡수**한다. “정합성 총량”이 아니라 **우선순위**: 1) 도메인 언어(DB·API·대화) 2) 패키지 구조 3) 기술 접미사.
- 테이블: `board` / `board_post` …, `user` / `user_signup_request` …
- 엔티티 패키지 예: `BoardEntity` → `board.group`, `UserEntity` → `user.account`. `group` 은 강한 도메인 의미보다 **충돌 흡수용 중립어**로 선택.

### 메뉴 관리
- "프론트: 사용자 의도를 수집하고, 화면 규격에 맞는 payload를 만든다. 서버: 정합성, 권한, 비즈니스 규칙, 최종 상태 전이를 책임진다. 즉: __프론트는 인터랙션, 서버는 판정이다.__"
- "프론트에서 비즈니스를 많이 먹기 시작하면: 브라우저 상태가 진실의 원천이 되어 버린다. 우회 호출이나 stale 화면을 막을 수 없다. 규칙이 화면 곳곳에 중복된다. 나중에 모바일/다른 UI가 붙으면 같은 규칙을 또 구현해야 한다."
- "이동 규칙은 서버에서 강제한다. 허용: MAIN끼리 순서 변경. SUB를 MAIN 아래로 이동. SUB를 SUB 아래로 이동. 금지: MAIN을 다른 메뉴 아래로 이동. NO_SUB 메뉴 아래로 드롭. 자기 자신/자기 자손 아래로 이동. protectedYn=Y 메뉴 이동."
- "라벨 수정 불가: 이건 UI가 아니라 서버 계약으로 박아야 한다. 수정 시 menuLabel이 들어와도 무시하거나, 기존 값과 다르면 예외를 던지는 게 맞다."
- "프론트는 “A를 B 밑으로 옮겼고, source/target 형제 순서는 이렇다”까지만 보내면 된다. 그걸 허용할지, 순환인지, NO_SUB인지, protected인지, 실제 upperMenuNo와 idx를 어떻게 반영할지는 서버가 판단해야 한다."

## 일정(schedule)

### 일정 대분류 코드 vs 휴가 세부 코드 (2026-07-21)

**결론**: `SCHEDULE_CD`(대분류)와 `VCATN_CD`(휴가 세부)를 **구분**한다. 범용 `SCHEDULE_DETAIL_CD`로 합치지 않는다.

**행 모델** (`schedule` 테이블):
- `schedule_cd` — 모든 일정에 필수. 공휴일·외근·휴가·생일·기타 등 **종류**.
- `vcatn_cd` — **조건부**. `schedule_cd=VCATN`일 때만 채우고, 그 외는 `NULL`(비휴가로 바꾸면 클리어).

예: 외근 → `OUTDT` + `vcatn_cd=NULL` / 연차 → `VCATN` + `ANNUAL`.

**왜 휴가 코드를 일정 코드와 나누나**:
- 묻는 질문이 다르다. 대분류는 「이 일정의 종류」, 휴가 세부는 「휴가라면 어떤 휴가」.
- 대분류는 달력 색·단일일(공휴일)·상세 생략(생일)·필터 토글 등 **시스템 분기**와 붙고, Java `ScheduleType` enum과 이름이 맞물린 **하이브리드**(코드=표시·시드, enum=동작).
- 휴가 세부는 제도(연차·반차·공가…)에 따라 **자주 늘고**, 하드 분기가 거의 없어 **공통코드 관리**가 맞다.

**기각한 대안**:
- `SCHEDULE_CD` + 범용 `SCHEDULE_DETAIL_CD` — 이름만 대칭이고, 실제로는 「VCATN일 때만 쓰는 세부」 규칙을 숨긴다. 다른 대분류에 세부가 없으면 빈 추상화 부채다.
- 휴가 세부를 대분류로 평탄화(ANNUAL 등을 `SCHEDULE_CD` 형제) — 「휴가 전체」 집계·필터·enum 분기가 세부마다 복제된다.

**관리 가능 범위**:
- `VCATN_CD`: 추가·이름·활성/비활성·i18n — 관리 가능.
- `SCHEDULE_CD`: 표시명·정렬·사용여부는 가능하되, **신규 대분류 추가는 배포 계약**(enum·색·검증 동시). 코드만 추가하면 `ScheduleType.valueOf` 등이 깨진다.

**이후 변경 부채**:
- 휴가 제도 변화 → 코드만으로 거의 무부채.
- 다른 대분류에 세부가 생기면 그때 `*_CD` 추가 또는 `detail_cd` 일반화. 지금 미리 합치지 않는 편이 낫다.
- 전제: 비휴가에 `vcatn_cd`를 남기지 않기, 제목으로 휴가 종류를 추론하지 않기. (화면·저장 계약은 `docs/migration/journal/screen-spec.md`·`interaction-spec.md` 일정 절.)

### 사용자별 휴무일 투영 (2026-07-21)

**결론**: 전역 공휴일·주말인 `JournalDayDto.isHolyday`와 사용자 참가 휴가를 합치지 않는다. 저널 일자에는 별도 `vacationDayStatus`(`NONE`, `FULL_DAY`, `AM_HALF`, `PM_HALF`, `UNKNOWN`)와 `vacationReasonList`를 투영한다.

- 원천은 현재 사용자가 `schedule_participant`로 참가한 `schedule_cd=VCATN` 일정이다. 공개 여부만으로 다른 사용자의 휴가를 가져오지 않는다.
- 기간은 `bgn_dt`·`end_dt` 양 끝을 포함해 각 저널 일자로 펼친다. 조회한 일자 범위 전체를 한 번의 overlap 쿼리로 가져오며 일자별 N+1 조회나 별도 캐시는 두지 않는다.
- `AM_HALF`·`PM_HALF`는 각각 반일 상태이며 같은 날짜에 둘 다 있으면 `FULL_DAY`로 합친다. `ANNUAL`, `PBLEN`, `CTSNN`, `MNSTR`, `UNPAID`는 전일이다.
- 기존 NULL 또는 정책에 등록되지 않은 신규 코드는 제목으로 추정하지 않고 `UNKNOWN`으로 드러내며 경고 로그를 남긴다. 따라서 휴가 세부 코드는 표시·관리 데이터가 기본이지만, **저널 휴무 시간 의미가 있는 신규 코드 추가는 `VacationDayStatus` 판정 정책도 함께 배포하는 계약**이다.
- 사유는 일정 본문이 아니라 제목을 중복 제거한 목록으로 노출한다. 본문은 일자 헤더에 싣기에는 길고 민감할 수 있으며, 제목은 사용자가 달력에서 의도적으로 정한 표시명이다.

---

## 인증·권한 (RBAC)

- 사용자 그룹 / Permission 축 설계·현행 계약: [`docs/spec/RBAC_USER_GROUP.md`](./RBAC_USER_GROUP.md).
- 시스템 롤(`USER`/`MNGR`/`DEV`)과 직교. 1차 소비처는 메뉴 `required_perm_key`.
- `auth_policy`는 로그인·세션 정책이며 RBAC 가드와 별 도메인이다.
