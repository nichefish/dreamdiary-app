package io.nicheblog.dreamdiary.infrastructure.log.model;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.type.LogType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;

/**
 * 통합 로그 입력 파라미터 (HTTP 활동 / 시스템·배치 공통).
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class LogParam
        extends BaseParam {

    /** 로그 고유 ID */
    private Integer logActvtysNo;

    /** 사용자 계정명 */
    private String username;

    /** trace ID */
    private String traceId;

    /** 로그 타입 */
    private LogType logType;

    /** 시그니처 */
    private String signature;

    /** 액션 구분 코드 */
    private String actionTyCd;

    /** 메소드 */
    private String httpMethod;
    /** URL */
    private String requestUri;
    /** HTTP status */
    private Integer httpStatus;

    /** ( */
    private Long durationMs;

    /** 파라미터 */
    private String param;

    /** 리퍼터 */
    private String referer;

    /** IP 주소 */
    private String ipAddr;

    /** 성공 여부 */
    private Boolean rslt;

    /** 결과 메세지 */
    private String rsltMsg;

    /** 내용 */
    private String content;

    /** 작업 카테고리 코드 */
    @Size(max = 50)
    protected String actvtyCtgrCd;

    /** 작업 카테고리 */
    @Size(max = 50)
    protected ActvtyCtgr actvtyCtgr;

    /** 작업자 이름 */
    private String userNm;

    /** 작업일시 */
    private String logDt;

    /** 익셉션 이름 */
    private String exceptionNm;

    /** 익셉션 메세지 */
    private String exceptionMsg;

    /* ----- */

    public LogParam() {
        this(true);
    }

    /**
     * @param bindRequestContext true면 현재 요청 컨텍스트에서 URI·메소드 등을 채움. 시스템 로그는 false.
     */
    private LogParam(final boolean bindRequestContext) {
        if (bindRequestContext) {
            this.setRequestAttr();
        }
    }

    /**
     * HTTP 컨텍스트 없이 기록하는 로그(배치·시스템). {@link LogType#SYSTEM}, 시스템 계정명이 기본으로 설정된다.
     */
    public static LogParam forSystem() {
        final LogParam p = new LogParam(false);
        p.setLogType(LogType.SYSTEM);
        p.setUsername(Constant.SYSTEM_ACNT);
        return p;
    }

    public static LogParam forSystem(final Boolean rslt) {
        final LogParam p = forSystem();
        p.setRslt(rslt);
        return p;
    }

    public static LogParam forSystem(final Boolean rslt, final String rsltMsg) {
        final LogParam p = forSystem();
        p.setRslt(rslt);
        p.setRsltMsg(rsltMsg);
        return p;
    }

    public static LogParam forSystem(final Boolean rslt, final String rsltMsg, final ActvtyCtgr actvtyCtgr) {
        final LogParam p = forSystem();
        p.setRslt(rslt);
        p.setRsltMsg(rsltMsg);
        p.setActvtyCtgr(actvtyCtgr);
        return p;
    }

    public LogParam(final Boolean rslt) {
        this();
        this.rslt = rslt;
    }

    public LogParam(final Boolean rslt, final String rsltMsg) {
        this();
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
    }

    public LogParam(final Boolean rslt, final String rsltMsg, final ActvtyCtgr actvtyCtgr) {
        this();
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
        this.actvtyCtgr = actvtyCtgr;
    }

    public LogParam(final String username, final Boolean rslt, final String rsltMsg, final ActvtyCtgr actvtyCtgr) {
        this();
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
        this.actvtyCtgr = actvtyCtgr;
        this.username = username;
    }

    public LogParam setResult(final boolean rslt, final String rsltMsg) {
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
        return this;
    }

    public LogParam setResult(final boolean rslt, final String rsltMsg, final ActvtyCtgr actvtyCtgr) {
        this.setResult(rslt, rsltMsg);
        this.actvtyCtgr = actvtyCtgr;
        return this;
    }

    public void setRequestAttr() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) return;

        final HttpServletRequest request = attr.getRequest();
        this.requestUri = request.getServletPath();
        this.httpMethod = request.getMethod();
        this.param = request.getQueryString();
        this.ipAddr = AuthUtils.getRemoteIpAddr();
    }

    public Boolean isAction(final String actionTyCd) {
        if (StringUtils.isEmpty(actionTyCd)) return false;
        return actionTyCd.equals(this.actionTyCd);
    }

    public void setExceptionInfo(final Throwable e) {
        this.exceptionNm = MessageUtils.getExceptionNm(e);
        this.exceptionMsg = MessageUtils.getExceptionMsg(e);
    }

    public ActvtyCtgr getActvtyCtgr() {
        if (this.actvtyCtgr != null) return this.actvtyCtgr;
        if (StringUtils.isEmpty(this.actvtyCtgrCd)) return ActvtyCtgr.DEFAULT;
        return ActvtyCtgr.valueOf(this.actvtyCtgrCd.toUpperCase());
    }
}
