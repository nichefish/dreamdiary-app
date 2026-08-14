# 릴리스 체크리스트 (버전업 전)

> DreamDiary 버전업(예: 0.25.0 → 0.26.0) 직전에 실행하는 재사용 체크리스트다.
> 위→아래 순서 권장. 각 게이트는 통과할 때까지 다음으로 넘어가지 않는다.
> SSOT·인코딩·수렴 원칙은 `AGENTS.md` 를 따른다. 릴리스별 특이사항은 맨 아래 "이번 릴리스" 섹션에 적는다.

## 0. 진입 전제
- [ ] 작업 브랜치가 릴리스 대상 브랜치인지 확인 (`git branch --show-current`)
- [ ] 워킹트리 clean (`git status`)

## 1. 히스토리·커밋 정리
- [ ] [커밋 Squash와 CHANGELOG 작성 방법론](COMMIT_SQUASH_CHANGELOG_GUIDE.md)에 따라 SAVEPOINT를 개념적으로 완결된 MILESTONE으로 수렴한다.
- [ ] detour/churn 커밋 스쿼시 — 같은 문제를 해결한 설계 선회·반복 수정·임시 계측을 결론 커밋으로 압축하고, 독립적으로 되돌릴 변화는 별도 MILESTONE으로 남긴다.
- [ ] squash 전 복구 참조와 tree object ID를 기록하고, squash 후 tree object ID 동일성과 `git diff <backup-ref> HEAD` = 0으로 내용 무변경을 검증한다.
- [ ] MILESTONE 커밋 메시지에 최종 결과·사용자 효과·핵심 계약·유효한 실측값을 보존한다.
- [ ] 커밋 메시지 한글 규칙(`AGENTS.md` §10) 준수 — 영문 subject·빈 메시지 reword. Conventional Commits 타입 접두사·스코프·식별자는 원문 유지.
- [ ] (선택) 전용 version-bump 커밋 — 과거 리추얼: `chore: bump to X`.
- [ ] 백업 브랜치는 전 게이트 통과 후 정리한다(그 전엔 남겨 둔다).

## 2. 게이트 (기계적 — 반드시 통과)
- [ ] 인코딩: `python scripts/check_encoding.py` (Windows: `py -3 ...`)
- [ ] 에이전트 룰 동기화: `python scripts/check_agent_rules_sync.py` — `AGENTS.md` ≡ `CLAUDE.md` ≡ `.cursor/rules/cursor.mdc` (byte 동일). 다르면 `python scripts/sync_agent_rules.py`.
- [ ] 프론트 빌드(type-check + vite): `./gradlew buildFrontend`
- [ ] 백엔드 빌드/테스트: `./gradlew test` (또는 `build`)
- [ ] generated source 최신 상태 확인 — MapStruct·QueryDSL 등 코드 생성기 사용 시, 엔티티/DTO 변경 후 재생성되었는지 확인. (해당 없으면 skip)
- [ ] 의존성 lock 변경 의도 확인 — `package-lock.json` / `gradle.lockfile` diff 가 이번 릴리스에서 의도한 변경인지 확인. 의도하지 않은 변경이 섞여 있으면 원인 파악 후 정리.

## 3. 기능 안정성 (QA)
- [ ] 이번 릴리스 핵심 기능의 **컴포넌트 배선** 수동 QA — 순수 로직이 아니라 상태 배선·전파 위주(과거 버그가 여기서 났다).
- [ ] 주요 CRUD 회귀 스모크 — 엔트리·챕터·리플렉션·스레드 등록/수정/삭제/검색.
- [ ] dev 서버 콘솔 에러·경고 없음 확인.

## 4. spec ↔ 소스 정합성 · 문서 보강
- [ ] 화면: `docs/migration/journal/screen-spec.md`
- [ ] 인터랙션: `docs/migration/journal/interaction-spec.md`
- [ ] 공통 컴포넌트: `docs/migration/common/component-spec.md`
- [ ] 저널 컴포넌트: `docs/migration/journal/component-spec.md`
- [ ] 각 파일을 실제로 열어 현황표기(✓ / ⚠ / ❌ / MISSING)가 현실과 일치하는지 확인. 변경 후 spec 이 현실을 정확히 기술하면 "갱신 불필요", 아니면 갱신.
- [ ] 주석 ↔ 코드 계약 일치 (현재 상태 서술 규칙). 동작 변경분은 주석·spec 을 변경 후 계약으로 갱신.
- [ ] 새로 추가/이동한 문서의 내부 링크·경로 정합 확인 — 파일명 변경·이동 후 참조가 깨지는 경우 수정.
- [ ] **문서 보강 (정합 확인을 넘어)** — 소스가 자란 영역에 spec 항목이 없거나 얇으면 이번 릴리스에서 채운다. 누락 기능은 먼저 spec에 기록한 뒤 반영(코드·주석·spec 동급 자산).

## 5. 테스트
- [ ] 신규/변경 기능에 테스트 추가 — 특히 커버 안 된 **이음새**(상태 배선·합성 로직).

## 6. 스키마·데이터
- [ ] **1.0 전까지 Flyway 증분 마이그레이션을 만들지 않는다.** 마스터 스키마만 도메인/엔티티와 일치하면 된다. 기존 마이그레이션 쿼리는 정리(삭제) 대상이며, 증분 마이그레이션은 **1.0부터** 누적한다.
- [ ] 마스터 스키마 정합 확인 — `app/backend/src/main/resources/schema/full/mariadb/schema-*.sql` (도메인 변경 반영: 예 `schema-journal-mariadb.sql`).
- [ ] 시드/필수 데이터 정합 — `app/backend/src/main/resources/schema/full/mariadb/data-required-*.sql`.

## 7. 릴리스 메타
- [ ] `docs/CHANGELOG.md` 항목 작성 — MILESTONE squash 완료 후 직전 릴리스 이후 **최신 커밋까지** 빠짐없이 검토한다. 커밋 메시지를 색인으로 삼고 최종 diff·spec·테스트·실측 결과를 대조한다.
- [ ] CHANGELOG는 개발 과정을 나열하지 않고 사용자·운영 관점의 확정 결과로 작성하며, 릴리스 문서용 마지막 독립 커밋으로 분리한다.
- [ ] 버전 일관성 확인 — 아래 위치의 버전 문자열이 모두 동일한지 대조:
  - `app/frontend-vue/package.json` (`version` 필드)
  - `app/frontend-vue/package-lock.json` (루트 `version`과 `packages[""]`의 `version`)
  - `build.gradle` (프로젝트 버전)
  - `config/application.yml` (`spring.flyway.target`)
  - `docs/CHANGELOG.md` (최신 헤딩)
- [ ] `config/application.yml`의 `spring.flyway.release-date`와 CHANGELOG 최신 헤딩 날짜가 일치하는지 대조.
- [ ] git tag 예정 이름 확인 — 예: `v0.27.0`. 기존 태그와 중복·오타 없는지 `git tag -l` 로 대조.

## 8. 코드 위생
- [ ] TODO/FIXME/HACK/XXX grep — 릴리스 차단 항목(미완성 로직, 임시 우회) 없는지 확인. 남겨도 되는 건 의도적으로 남긴 것임을 판단 후 통과.
- [ ] 디버그 코드 제거 확인 — `console.log`, `System.out.println`, `printStackTrace`, debug flag, 임시 버튼 등 프로덕션에 불필요한 코드 grep.
- [ ] dead file 제거 확인 — 이번 릴리스 작업으로 더 이상 사용하지 않는 파일(`*_old.*`, 미참조 컴포넌트, 빈 유틸 등) 정리 여부 확인. `import`/`require` 역참조로 검증.

## 9. 소스코드 고고학 훑어보기
> 방법론: [CODE_ARCHAEOLOGY_ADDENDUM.md](references/CODE_ARCHAEOLOGY_ADDENDUM.md). 원천: [REPO_HISTORY.md](references/dreamdiary/REPO_HISTORY.md) · 정적: [REPO_STATIC_ANALYSIS.md](references/dreamdiary/REPO_STATIC_ANALYSIS.md) · 진단: [system-issues.md](references/dreamdiary/system-issues.md).
> 산출물은 스냅샷이다. 허브 파일이 분해·삭제됐거나 시대가 바뀌었으면 표를 고치지 말고 같은 역할의 문서를 재작성한다(부록 §14).

- [ ] 역사서 통섭이 이번 릴리스 방향과 어긋나는지 확인. 어긋나면 최소 패스 대신 재작성.
- [ ] 이번 릴리스가 남긴 부채 — dead/zombie, 허브 축적, 평행 표면, 스키마 baseline drift.
- [ ] 정적분석 PRESSURE의 파일명이 아직 트리에 있는지 확인. 없으면 재작성.
- [ ] 진단 매트릭스에 해소된 항이 남아 있으면 `system-issues.md`를 재작성(줄 수정으로 연명하지 않음).

## 10. 최종 저장소 점검
> push 직전 `git diff` 한 패스로 아래를 모두 확인한다.

- [ ] 한글 주석·헤더 보존 — 인코딩 게이트가 못 잡는 일괄 `?` 치환 유무 육안 확인.
- [ ] 개인정보·실명 유출 없음 — 테스트·스펙·예시·fixture·커밋 메시지에 실명·호칭·대화 인용·저널 스니펫 없는지 저장소 grep. 원격에 올라가면 히스토리 정리 사고.
- [ ] main 머지 / 태그 / PR.
- [ ] 검증 완료 후 백업 브랜치 삭제.

---

## 이번 릴리스 (버전별 특이사항 기록)

> 매 릴리스마다 이 섹션만 갱신한다. 위 항목 중 이번에 특별히 챙길 것·현재 상태를 적는다.

### v0.27.0
- 기능 배선 QA: 기본 진입 `/journal/daily`, 일간 뷰 탭·미니 캘린더·일자 태그 클라우드, 팝업 모드 유지 여부를 확인한다.
- AI 설정 QA: 관리자 AI 탭의 저널 임베딩 ON/OFF 저장·실패 복원과, OFF 상태에서 엔트리 등록·수정 시 embedding/entity queue를 건너뛰는지 확인한다.
- 성능·캐시 QA: 에디터·태그·차트 지연 로딩, 정적 자산 immutable cache, API GET ETag/304, 인증 조회 재사용이 사용자 상태를 오염시키지 않는지 확인한다.
- 의존성 lock: `element-plus`·`yup` 제거에 따른 `package-lock.json` 축소는 의도된 번들 정리다.
- 스키마: Flyway 증분 없음(1.0 전). 마스터 스키마와 런타임 entity 정합을 확인한다.
- 태그: `v0.27.0`은 dev 브랜치가 아니라 main 머지 커밋에 생성한다. 과거 릴리스 태그 백필은 각 main 머지 커밋 대조 후 별도로 수행한다.
