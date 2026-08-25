# 저널 FLOW → 스레드 소속 수렴 실행 기록

> 상태: 완료된 migration 실행 기록. 현재 동작 계약은 [`DESIGN_NOTES.md`](../../spec/DESIGN_NOTES.md)와 저널 화면·인터랙션·컴포넌트 spec을 따른다.

## 목적

`related_content`의 FLOW 연결 그래프를 `journal_thread_entry` N:M 소속 구조로 수렴시킨 실행 결과를 보존한다.

## 실행 결과

- `V0.24.7`: `journal_thread_entry` 소속 구조와 백엔드 API를 적용했다.
- `V0.24.8`: FLOW 간선 29건, 엔트리 33개, 연결 컴포넌트 4개를 스레드 4건과 소속 33건으로 이관했다.
- `V0.24.11`: 이관이 끝난 FLOW 행 29건을 소프트 삭제했다.
- 백엔드 FLOW DTO·서비스·엔드포인트·enum과 프론트 FLOW 요약·연결·종단 보기 경로를 제거했다. 저널 엔트리의 흐름 조직화는 스레드 소속을 단일 경로로 사용한다.

## 운영 기준

- `V0.24.7`·`V0.24.8`·`V0.24.11`은 운영 DB에 직접 반영한 일회성 작업 식별자다. 적용 검증 후 SQL 파일을 저장소에서 제거했으므로 migration 디렉터리에 파일이 없는 상태는 미완료 작업이 아니다.
- 새 환경의 최종 구조 SSOT는 `schema/full/mariadb/schema-journal-mariadb.sql`의 `journal_thread_entry` 정의다.
