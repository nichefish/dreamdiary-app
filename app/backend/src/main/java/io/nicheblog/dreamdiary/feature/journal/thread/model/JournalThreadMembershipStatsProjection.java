package io.nicheblog.dreamdiary.feature.journal.thread.model;

import java.time.LocalDate;

/**
 * JournalThreadMembershipStatsProjection
 * <pre>
 *  스레드 목록 enrich 용 소속 집계 Projection.
 *  활성 소속 수와 소속 엔트리 기준일 min/max 를 엔트리 풀 로드 없이 조회한다.
 * </pre>
 *
 * @author nichefish
 */
public interface JournalThreadMembershipStatsProjection {

    /** 스레드 ID */
    Integer getThreadId();

    /** 활성 소속 엔트리 수 */
    Number getMembershipCount();

    /** 소속 엔트리 기준일 최소값 */
    LocalDate getFirstEntryDate();

    /** 소속 엔트리 기준일 최대값 */
    LocalDate getLastEntryDate();
}