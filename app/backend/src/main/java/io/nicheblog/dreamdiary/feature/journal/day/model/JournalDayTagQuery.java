package io.nicheblog.dreamdiary.feature.journal.day.model;

import org.apache.commons.lang3.StringUtils;

/**
 * JournalDayTagQuery
 * <pre>
 *  Journal day tag period query.
 * </pre>
 */
public record JournalDayTagQuery(
        Integer yy,
        Integer mnth,
        String weekStartDt,
        String stdrdDt
) {

    /**
     * 연/월 기반 태그 질의를 생성한다.
     *
     * @param yy 연도
     * @param mnth 월
     * @return 태그 질의
     */
    public static JournalDayTagQuery of(final Integer yy, final Integer mnth) {
        return new JournalDayTagQuery(yy, mnth, null, null);
    }

    /**
     * 주 시작일 기반 태그 질의를 생성한다.
     *
     * @param weekStartDt 주 시작일
     * @return 태그 질의
     */
    public static JournalDayTagQuery weekly(final String weekStartDt) {
        return new JournalDayTagQuery(null, null, weekStartDt, null);
    }

    /**
     * 기준일자 하루에 한정된 태그 질의를 생성한다.
     *
     * @param stdrdDt 기준일자
     * @return 태그 질의
     */
    public static JournalDayTagQuery daily(final String stdrdDt) {
        return new JournalDayTagQuery(null, null, null, stdrdDt);
    }

    /**
     * 주 시작일 조건 보유 여부를 반환한다.
     *
     * @return 주 시작일 조건 보유 여부
     */
    public boolean hasWeekStartDt() {
        return StringUtils.isNotBlank(weekStartDt);
    }

    /**
     * 연/월 조건 보유 여부를 반환한다.
     *
     * @return 연/월 조건 보유 여부
     */
    public boolean hasYyMnth() {
        return yy != null && mnth != null;
    }

    /**
     * 기준일자 조건 보유 여부를 반환한다.
     *
     * @return 기준일자 조건 보유 여부
     */
    public boolean hasStdrdDt() {
        return StringUtils.isNotBlank(stdrdDt);
    }
}
