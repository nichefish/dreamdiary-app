package io.nicheblog.dreamdiary.feature.journal.day.type;

/**
 * JournalDayViewType
 *
 * @author nichefish
 */
public enum JournalDayViewType {
    LIST,
    CAL,
    DAILY,
    WEEKLY,
    SEARCH;

    /**
     * 대소문자 구분 없이 문자 치환
     * @param value String
     * @return enum
     */
    public static JournalDayViewType from(final String value) {
        return JournalDayViewType.valueOf(value.toUpperCase());
    }
}
