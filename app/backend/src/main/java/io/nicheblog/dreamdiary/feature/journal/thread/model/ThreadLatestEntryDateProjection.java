package io.nicheblog.dreamdiary.feature.journal.thread.model;

import java.time.LocalDate;

/**
 * ThreadLatestEntryDateProjection
 * <pre>
 *  스레드별 소속 엔트리 최신 일기 날짜 집계 Projection.
 * </pre>
 *
 * @author nichefish
 */
public interface ThreadLatestEntryDateProjection {

    /**
     * 스레드 PK.
     *
     * @return 스레드 id
     */
    Integer getThreadId();

    /**
     * 소속 엔트리(일기/꿈/노트)의 일기 날짜({@code journal_day.journal_date}) 최대값.
     * 소속 엔트리가 없거나 날짜가 없는 스레드는 이 집계 결과에 포함되지 않는다(호출측에서 null=뒤로 처리).
     *
     * @return 소속 엔트리 최신 일기 날짜
     */
    LocalDate getLatestDate();
}
