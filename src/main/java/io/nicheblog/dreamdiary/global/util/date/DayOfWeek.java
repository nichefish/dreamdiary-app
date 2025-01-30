package io.nicheblog.dreamdiary.global.util.date;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * DayOfWeek
 * <pre>
 *  요일 정보
 * </pre>
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum DayOfWeek {

    SUNDAY(1, "일", "日"),
    MONDAY(2, "월", "月"),
    TUESDAY(3, "화", "火"),
    WEDNESDAY(4, "수", "水"),
    THURSDAY(5, "목", "木"),
    FRIDAY(6, "금", "金"),
    SATURDAY(7, "토", "土");

    public final Integer idx;
    public final String kor;
    public final String chinese;

    /* ----- */

    /**
     * 인덱스 받아서 한글 반환
     */
    public static String asKorean(final Integer idx) {
        for (final DayOfWeek day : DayOfWeek.values()) {
            if (Objects.equals(day.idx, idx)) return day.kor;
        }
        throw new IllegalArgumentException("Invalid dayofweek index: " + idx);
    }

    /**
     * 인덱스 받아서 한자 반환
     */
    public static String asChinese(final Integer idx) {
        for (final DayOfWeek day : DayOfWeek.values()) {
            if (Objects.equals(day.idx, idx)) return day.chinese;
        }
        throw new IllegalArgumentException("Invalid dayofweek index: " + idx);
    }
}