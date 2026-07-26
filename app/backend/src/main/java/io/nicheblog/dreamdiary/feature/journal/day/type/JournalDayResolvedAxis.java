package io.nicheblog.dreamdiary.feature.journal.day.type;

/**
 * JournalDayResolvedAxis
 * <pre>
 *  저널 일자 완결(쓰기 잠금) 축. 일기/노트와 꿈은 서로 독립이다.
 * </pre>
 *
 * @author nichefish
 */
public enum JournalDayResolvedAxis {

    /** 일기·노트 챕터/엔트리/해석/댓글/관련·lifecycle·state */
    DIARY,
    /** 꿈 등록·엔트리/해석/댓글/관련·lifecycle·state */
    DREAM
}
