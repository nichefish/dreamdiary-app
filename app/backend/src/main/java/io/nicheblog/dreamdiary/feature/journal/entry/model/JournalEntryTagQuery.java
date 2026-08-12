package io.nicheblog.dreamdiary.feature.journal.entry.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 엔트리 태그클라우드의 콘텐츠 타입과 집계 기간을 표현한다.
 * 연/월·주 시작일·기준일자 중 하나를 사용하며 캐시 키의 기간 계약으로도 사용된다.
 */
public record JournalEntryTagQuery(
        ContentType contentType,
        Integer yy,
        Integer mnth,
        String weekStartDt,
        String stdrdDt
) {

    public JournalEntryTagQuery {
        Objects.requireNonNull(contentType, "contentType");
    }

    /**
     * 연/월 기반 태그 질의를 생성한다.
     *
     * @param contentType 콘텐츠 타입
     * @param yy 연도
     * @param mnth 월
     * @return 태그 질의 객체
     */
    public static JournalEntryTagQuery of(final ContentType contentType, final Integer yy, final Integer mnth) {
        return new JournalEntryTagQuery(contentType, yy, mnth, null, null);
    }

    /**
     * 주차 시작일 기반 태그 질의를 생성한다.
     *
     * @param contentType 콘텐츠 타입
     * @param weekStartDt 주 시작일
     * @return 태그 질의 객체
     */
    public static JournalEntryTagQuery weekly(final ContentType contentType, final String weekStartDt) {
        return new JournalEntryTagQuery(contentType, null, null, weekStartDt, null);
    }

    /**
     * 기준일자 하루에 한정된 태그 질의를 생성한다.
     *
     * @param contentType 콘텐츠 타입
     * @param stdrdDt 기준일자
     * @return 태그 질의 객체
     */
    public static JournalEntryTagQuery daily(final ContentType contentType, final String stdrdDt) {
        return new JournalEntryTagQuery(contentType, null, null, null, stdrdDt);
    }

    /**
     * 주 시작일 조건 보유 여부를 확인한다.
     *
     * @return 보유 여부
     */
    public boolean hasWeekStartDt() {
        return StringUtils.isNotBlank(weekStartDt);
    }

    /**
     * 연/월 조건 보유 여부를 확인한다.
     *
     * @return 보유 여부
     */
    public boolean hasYyMnth() {
        return yy != null && mnth != null;
    }

    /**
     * 기준일자 조건 보유 여부를 확인한다.
     *
     * @return 기준일자 조건 보유 여부
     */
    public boolean hasStdrdDt() {
        return StringUtils.isNotBlank(stdrdDt);
    }

    /**
     * 기간 조건(기준일자, 주 시작일 또는 연/월) 보유 여부를 확인한다.
     *
     * @return 기간 조건 보유 여부
     */
    public boolean hasPeriod() {
        return hasStdrdDt() || hasWeekStartDt() || hasYyMnth();
    }
}
