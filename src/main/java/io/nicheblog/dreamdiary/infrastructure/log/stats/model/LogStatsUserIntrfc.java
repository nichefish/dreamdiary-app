package io.nicheblog.dreamdiary.infrastructure.log.stats.model;

/**
 * LogStatsUserIntrfc
 * <pre>
 *  (사용자별) 로그 통계 조회용 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface LogStatsUserIntrfc {

    /** 계정명 */
    String getUsername();

    /** 이름 */
    String getUserNm();

    /** 아이디 */
    String getUserInfoNo();

    /** 아이디 */
    String getRetireYn();

    /** 역할 키 */
    String getRoleKey();

    /** 역할 표시명 */
    String getRoleName();

    /** 로그 목록 건수 */
    Long getActvtyCnt();
}
