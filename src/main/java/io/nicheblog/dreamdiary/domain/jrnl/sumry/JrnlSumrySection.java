package io.nicheblog.dreamdiary.domain.jrnl.sumry;

import io.nicheblog.dreamdiary.domain.jrnl.day.JrnlDayViewType;

/**
 * JrnlDayViewType
 *
 * @author nichefish
 */
public enum JrnlSumrySection {
    DREAM,
    DIARY;

    /**
     * 대소문자 구분 없이 문자 치환
     * @param value String
     * @return enum
     */
    public static JrnlSumrySection from(final String value) {
        return JrnlSumrySection.valueOf(value.toUpperCase());
    }
}
