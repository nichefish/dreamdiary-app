package io.nicheblog.dreamdiary.feature.journal.sumry.type;

/**
 * JournalSumryTagType
 *
 * @author nichefish
 */
public enum JournalSumryTagType {
    DAY,
    DREAM,
    DIARY;

    /**
     * 대소문자 구분 없이 문자 치환
     * @param value String
     * @return enum
     */
    public static JournalSumryTagType from(final String value) {
        return JournalSumryTagType.valueOf(value.toUpperCase());
    }
}
