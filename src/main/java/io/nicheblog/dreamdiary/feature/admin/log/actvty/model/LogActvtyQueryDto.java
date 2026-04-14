package io.nicheblog.dreamdiary.feature.admin.log.actvty.model;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.LogType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;

/**
 * LogActvtyQueryDto
 * <pre>
 *  활동 로그 조회 전용 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class LogActvtyQueryDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    /** 로그 고유 ID */
    private Integer id;
    /** 작업자 계정명 */
    private String username;
    /** 작업자 이름 */
    private String logUserNm;
    /** 권한코드 */
    private String authCd;
    /** 권한명 */
    private String authNm;
    /** 작업일시 */
    private String logDt;

    /** 작업 구분 코드 (ex. 게시판, 공지사항, ...) (기능/모듈 단위) */
    private String actvtyCtgrCd;
    /** 작업 구분 코드 (ex. 게시판, 공지사항, ...) (기능/모듈 단위) */
    private String actvtyCtgrNm;

    /** trace ID */
    private String traceId;

    /** HTTP 메소드 */
    private String httpMethod;
    /** 작업 URL */
    private String requestUri;
    /** 작업 파라미터 */
    private String param;
    /** 작업 파라미터 맵 */
    private HashMap<String, String> paramMap;

    /** 로그 타입 */
    private LogType logType;

    /** 시그니처 */
    private String signature;

    /** 작업 내용 */
    private String cn;

    /** 작업자 IP */
    private String ipAddr;
    /** 리퍼러 */
    private String referer;

    /** 작업 결과 */
    private String rslt;
    /** 작업 결과 메세지 */
    private String rsltMsg;
    /** 익셉션 이름 */
    private String exceptionNm;
    /** 익셉션 메세지 */
    private String exceptionMsg;
    /** 소요시간 (ms) */
    private Long durationMs;

    /* ----- */

    /**
     * Getter :: 성공여부
     *
     * @return {@link Boolean} -- 성공여부 반환
     */
    public Boolean isSuccess() {
        return "true".equals(this.rslt);
    }

    /**
     * Getter :: 작업자 여부
     */
    public Boolean getIsActvtyUser() {
        return AuthUtils.isRegstr(this.username);
    }

    /**
     * Getter :: 작업 파라미터 = "null"은 표시하지 않음
     */
    public String getParam() {
        if ("null".equals(this.param)) return "-";
        return this.param;
    }

    @Override
    public Integer getKey() {
        return this.id;
    }

}
