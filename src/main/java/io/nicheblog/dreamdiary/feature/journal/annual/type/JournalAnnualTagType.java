package io.nicheblog.dreamdiary.feature.journal.annual.type;

/**
 * JournalAnnualTagType
 *
 * @author nichefish
 */
public enum JournalAnnualTagType {
    DAY,
    DREAM,
    DIARY;

    /**
     * 대소문자 구분 없이 문자 치환
     * @param value String
     * @return enum
     */
    public static JournalAnnualTagType from(final String value) {
        return JournalAnnualTagType.valueOf(value.toUpperCase());
    }
}
