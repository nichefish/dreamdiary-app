package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 저널 엔트리와 임베딩 작업 테이블의 수동 동기화 결과 DTO입니다.
 */
@Getter
@Builder
public class JournalEntryEmbeddingSyncResultDto {

    /** 동기화 기준이 된 활성 저널 엔트리 건수입니다. */
    private final long activeEntryCount;

    /** 동기화 전 활성 임베딩 작업 건수입니다. */
    private final long activeEmbeddingCountBefore;

    /** 새로 생성한 임베딩 작업 건수입니다. */
    private final long created;

    /** 본문 변경 또는 미완료 상태로 인해 다시 대기 상태로 돌린 작업 건수입니다. */
    private final long requeued;

    /** 이미 엔트리와 임베딩이 같은 해시로 맞아 있어 유지한 작업 건수입니다. */
    private final long unchanged;

    /** 제목/본문이 없어 벡터화를 건너뛴 작업 건수입니다. */
    private final long skipped;

    /** 활성 엔트리가 없어 검색 대상에서 제거한 임베딩 작업 건수입니다. */
    private final long removed;

    /** 동기화 후 활성 임베딩 작업 건수입니다. */
    private final long activeEmbeddingCountAfter;
}
