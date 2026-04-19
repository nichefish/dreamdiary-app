package io.nicheblog.dreamdiary.feature.journal.annual.type;

/**
 * JournalDayViewType
 *
 * @author nichefish
 */
public enum JournalAnnualSection {
    DREAM,
    DIARY;

    /**
     * 대소문자 구분 없이 문자 치환
     * @param value String
     * @return enum
     */
    public static JournalAnnualSection from(final String value) {
        return JournalAnnualSection.valueOf(value.toUpperCase());
    }
}
