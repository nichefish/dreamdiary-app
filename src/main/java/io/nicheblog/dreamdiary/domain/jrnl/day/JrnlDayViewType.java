package io.nicheblog.dreamdiary.domain.jrnl.day;

/**
 * JrnlDayViewType
 *
 * @author nichefish
 */
public enum JrnlDayViewType {
    LIST,
    CAL,
    DAILY;

    /**
     * 대소문자 구분 없이 문자 치환
     * @param value String
     * @return enum
     */
    public static JrnlDayViewType from(final String value) {
        return JrnlDayViewType.valueOf(value.toUpperCase());
    }
}