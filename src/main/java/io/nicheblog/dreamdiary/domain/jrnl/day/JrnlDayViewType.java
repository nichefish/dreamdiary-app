package io.nicheblog.dreamdiary.domain.jrnl.day;

/**
 * JrnlDayViewType
 *
 * @author nichefish
 */
public enum JrnlDayViewType {
    LIST,
    CAL;

    public static JrnlDayViewType from(final String value) {
        return JrnlDayViewType.valueOf(value.toUpperCase());
    }
}