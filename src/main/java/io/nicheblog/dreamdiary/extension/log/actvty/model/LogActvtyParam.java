package io.nicheblog.dreamdiary.extension.log.actvty.model;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.sys.model.LogSysParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
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
 * LogActvtyParam
 * <pre>
 *  활동 로그 파라미터.
 *  (화면에서 넘어오는 인자 정리 위한 클래스)
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class LogActvtyParam
        extends BaseParam {

    /** 로그 고유 ID */
    private Integer logActvtysNo;

    /** 사용자 ID */
    private String userId;

    /** 액션 구분 코드 */
    private String actionTyCd;

    /** URL */
    private String url;

    /** 메소드 */
    private String mthd;

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
    private String cn;

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

    /**
     * 생성자.
     * 객체 생성시 request 관련 인자들 자동 세팅한다. (가능하면)
     */
    public LogActvtyParam() {
        this.setRequestAttr();
    }

    /**
     * 생성자. 객체 생성시 request 관련 인자들을 자동 세팅한다. (가능하면)
     */
    public LogActvtyParam(final Boolean rslt) {
        this();
        this.rslt = rslt;
    }

    /**
     * 생성자. 객체 생성시 request 관련 인자들을 자동 세팅한다. (가능하면)
     *
     * @param rslt 결과
     * @param rsltMsg 결과 메세지
     */
    public LogActvtyParam(final Boolean rslt, final String rsltMsg) {
        this();
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
    }

    /**
     * 생성자. 객체 생성시 request 관련 인자들을 자동 세팅한다. (가능하면)
     *
     * @param rslt 결과
     * @param rsltMsg 결과 메세지
     * @param actvtyCtgr 활동 유형
     */
    public LogActvtyParam(final Boolean rslt, final String rsltMsg, final ActvtyCtgr actvtyCtgr) {
        this();
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
        this.actvtyCtgr = actvtyCtgr;
    }

    /**
     * 생성자. 객체 생성시 request 관련 인자들을 자동 세팅한다. (가능하면)
     *
     * @param userId 사용자 ID. (비로그인 처리시 사용)
     * @param rslt 결과
     * @param rsltMsg 결과 메세지
     * @param actvtyCtgr 활동 유형
     */
    public LogActvtyParam(final String userId, final Boolean rslt, final String rsltMsg, final ActvtyCtgr actvtyCtgr) {
        this();
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
        this.actvtyCtgr = actvtyCtgr;
        this.userId = userId;
    }

    /**
     * 활동 로그 파라미터 객체에 결과 값과 메시지를 설정합니다.
     *
     * @param rslt 결과 값 (성공 여부)
     * @param rsltMsg 결과 메시지
     * @return {@link LogSysParam} -- 결과가 설정된 현재 객체
     */
    public LogActvtyParam setResult(final boolean rslt, final String rsltMsg) {
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
        return this;
    }

    /**
     * 활동 로그 로그 파라미터 객체에 결과 값과 메시지를 설정합니다.
     *
     * @param rslt 결과 값 (성공 여부)
     * @param rsltMsg 결과 메시지
     * @param actvtyCtgr 활동 카테고리
     * @return {@link LogSysParam} -- 결과가 설정된 현재 객체
     */
    public LogActvtyParam setResult(final boolean rslt, final String rsltMsg, final ActvtyCtgr actvtyCtgr) {
        this.setResult(rslt, rsltMsg);
        this.actvtyCtgr = actvtyCtgr;
        return this;
    }

    /**
     * request 관련 내용들
     */
    public void setRequestAttr() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // request 관련 인자들 미리 세팅 (가능하다면)
        if (attr == null) return;

        final HttpServletRequest request = attr.getRequest();
        this.url = request.getServletPath();         // 작업 url
        this.mthd = request.getMethod();           // 접근 메소드
        this.param = request.getQueryString();       // 작업 파라미터
        this.referer = request.getHeader(Constant.REFERER);      // 리퍼러
        this.ipAddr = AuthUtils.getAcsIpAddr();       // 작업 IP
    }

    /**
     * 액션유형을 체크한다.
     */
    public Boolean isAction(final String actionTyCd) {
        if (StringUtils.isEmpty(actionTyCd)) return false;
        return actionTyCd.equals(this.actionTyCd);
    }

    /**
     * Exception 정보를 객체에 세팅한다.
     *
     * @param e 예외(exception, error) 정보 객체
     */
    public void setExceptionInfo(final Throwable e) {
        this.exceptionNm = MessageUtils.getExceptionNm(e);
        this.exceptionMsg = MessageUtils.getExceptionMsg(e);
    }

    /**
     * Getter :: 작업 카테고리 조회
     *
     * @return {@link ActvtyCtgr} -- 작업 카테고리 (기본값: ActvtyCtgr.DEFAULT)
     */
    public ActvtyCtgr getActvtyCtgr() {
        if (this.actvtyCtgr != null) return this.actvtyCtgr;
        if (StringUtils.isEmpty(this.actvtyCtgrCd)) return ActvtyCtgr.DEFAULT;
        return ActvtyCtgr.valueOf(this.actvtyCtgrCd.toUpperCase());
    }
}
