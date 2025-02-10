package io.nicheblog.dreamdiary.extension.log.stats.model;

/**
 * LogStatsUserIntrfc
 * <pre>
 *  (사용자별) 로그 통계 조회용 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface LogStatsUserIntrfc {

    /** 아이디 */
    String getUserId();

    /** 이름 */
    String getUserNm();

    /** 아이디 */
    String getUserInfoNo();

    /** 아이디 */
    String getRetireYn();

    /** 아이디 */
    String getAuthCd();

    /** 아이디 */
    String getAuthNm();

    /** 로그 목록 건수 */
    Long getActvtyCnt();
}