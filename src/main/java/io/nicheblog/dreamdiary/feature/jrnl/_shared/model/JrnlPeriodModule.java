package io.nicheblog.dreamdiary.feature.jrnl._shared.model;

/**
 * JrnlTagPeriodModule
 * <pre>
 *  저널 처리에 필요한 연/월 정보를 제공하는 모듈 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlPeriodModule {

    /** 연도 */
    Integer getYy();

    /** 월 */
    Integer getMnth();
}
