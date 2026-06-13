package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * 관리자 화면에 표시할 저널 엔트리 임베딩 작업 통계 DTO입니다.
 */
@Getter
@Builder
public class JournalEntryEmbeddingStatsDto {

    /** 활성 저널 entry 전체 건수입니다. Admin UI의 Total 기준입니다. */
    private final long total;

    /** 임베딩 작업 큐 row 건수입니다. */
    private final long queueRows;

    /** 아직 큐 row가 없는 entry 건수입니다. */
    private final long unqueuedEntries;

    /** 아직 처리 대기 중인 작업 건수입니다. */
    private final long pending;

    /** 워커가 선점해 처리 중인 작업 건수입니다. */
    private final long processing;

    /** 벡터 생성이 완료된 작업 건수입니다. */
    private final long embedded;

    /** 벡터 생성 중 실패한 작업 건수입니다. */
    private final long failed;

    /** 원문이 비어 있는 등의 이유로 건너뛴 작업 건수입니다. */
    private final long skipped;

    /** 아직 종료 상태에 도달하지 않은 작업 건수입니다. */
    private final long remaining;

    /** 성공, 실패, 스킵을 포함해 종료된 작업 건수입니다. */
    private final long completed;

    /** 전체 entry 중 벡터/종료 처리가 끝난 entry 비율입니다. */
    private final double completionRate;

    /** 전체 entry 중 실제 벡터 생성에 성공한 entry 비율입니다. */
    private final double vectorizedRate;

    /** 큐 row 기준 종료 비율입니다. */
    private final double queueCompletionRate;

    private final boolean syncRunning;
    private final String syncPhase;
    private final long syncProcessed;
    private final long syncTotal;
    private final Date syncStartedAt;
    private final Date syncFinishedAt;
    private final JournalEntryEmbeddingSyncResultDto syncResult;
    private final String syncErrorMessage;

    public JournalEntryEmbeddingStatsDto withSyncStatus(final JournalEntryEmbeddingSyncJobStatusDto syncStatus) {
        if (syncStatus == null) return this;

        return JournalEntryEmbeddingStatsDto.builder()
                .total(total)
                .queueRows(queueRows)
                .unqueuedEntries(unqueuedEntries)
                .pending(pending)
                .processing(processing)
                .embedded(embedded)
                .failed(failed)
                .skipped(skipped)
                .remaining(remaining)
                .completed(completed)
                .completionRate(completionRate)
                .vectorizedRate(vectorizedRate)
                .queueCompletionRate(queueCompletionRate)
                .syncRunning(syncStatus.isRunning())
                .syncPhase(syncStatus.getPhase())
                .syncProcessed(syncStatus.getProcessed())
                .syncTotal(syncStatus.getTotal())
                .syncStartedAt(syncStatus.getStartedAt())
                .syncFinishedAt(syncStatus.getFinishedAt())
                .syncResult(syncStatus.getResult())
                .syncErrorMessage(syncStatus.getErrorMessage())
                .build();
    }
}
