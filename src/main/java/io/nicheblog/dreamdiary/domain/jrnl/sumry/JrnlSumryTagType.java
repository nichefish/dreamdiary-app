package io.nicheblog.dreamdiary.domain.jrnl.sumry;

/**
 * JrnlSumryTagType
 *
 * @author nichefish
 */
public enum JrnlSumryTagType {
    DAY,
    DREAM,
    DIARY;

    /**
     * 대소문자 구분 없이 문자 치환
     * @param value String
     * @return enum
     */
    public static JrnlSumryTagType from(final String value) {
        return JrnlSumryTagType.valueOf(value.toUpperCase());
    }
}
