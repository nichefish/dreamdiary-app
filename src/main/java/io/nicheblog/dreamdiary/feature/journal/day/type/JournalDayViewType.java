package io.nicheblog.dreamdiary.feature.journal.day.type;

/**
 * JournalDayViewType
 * 저널 일자 화면의 보기 방식(목록·달력·일간 등)을 구분한다.
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
     * 대소문자를 무시하고 문자열을 enum 상수로 변환한다.
     *
     * @param value 요청 파라미터 등에서 넘어온 보기 방식 문자열
     * @return 대응하는 {@link JournalDayViewType}
     */
    public static JournalDayViewType from(final String value) {
        return JournalDayViewType.valueOf(value.toUpperCase());
    }
}
