package io.nicheblog.dreamdiary.feature.journal.sumry.type;

/**
 * JournalDayViewType
 *
 * @author nichefish
 */
public enum JournalSumrySection {
    DREAM,
    DIARY;

    /**
     * 대소문자 구분 없이 문자 치환
     * @param value String
     * @return enum
     */
    public static JournalSumrySection from(final String value) {
        return JournalSumrySection.valueOf(value.toUpperCase());
    }
}
