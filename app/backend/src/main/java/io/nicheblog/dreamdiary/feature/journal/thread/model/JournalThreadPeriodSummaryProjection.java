package io.nicheblog.dreamdiary.feature.journal.thread.model;

import java.time.LocalDate;

/**
 * JournalThreadPeriodSummaryProjection
 * <pre>
 *  월간·주간 저널 화면의 기간별 스레드 집계 Projection.
 *  화면 필터가 적용된 일자 DTO를 재집계하지 않고 DB의 활성 소속을 기간 기준으로 집계한다.
 * </pre>
 *
 * @author nichefish
 */
public interface JournalThreadPeriodSummaryProjection {

    /** 스레드 ID */
    Integer getThreadId();

    /** 스레드 제목 */
    String getTitle();

    /** 조회 기간 안의 소속 엔트리 수 */
    Number getEntryCount();

    /** 조회 기간 안에서 스레드가 처음 등장한 엔트리 일자 */
    LocalDate getFirstEntryDate();
}
