package io.nicheblog.dreamdiary.extension.log.sys.model;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * LogSysParam
 * <pre>
 *  시스템 로그 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LogSysParam
        extends BaseParam {

    /** 로그 고유 ID */
    private Integer logSysNo;

    /** 작업자 ID = 시스템 계정 */
    @Builder.Default
    private String userId = Constant.SYSTEM_ACNT;

    /** 성공 여부 */
    private Boolean rslt;

    /** 결과 메세지 */
    private String rsltMsg;

    /** 내용 */
    private String cn;

    /** 작업 카테고리 코드 */
    private ActvtyCtgr actvtyCtgr;

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
     *
     * @param rslt 처리 결과 상태를 나타내는 Boolean 값
     */
    public LogSysParam(final Boolean rslt) {
        this.rslt = rslt;
    }

    /**
     * 생성자.
     *
     * @param rslt 처리 결과 상태를 나타내는 Boolean 값
     * @param rsltMsg 처리 결과 메시지
     */
    public LogSysParam(final Boolean rslt, final String rsltMsg) {
        this(rslt);
        this.rsltMsg = rsltMsg;
    }

    /**
     * 생성자.
     *
     * @param rslt 처리 결과 상태를 나타내는 Boolean 값
     * @param rsltMsg 처리 결과 메시지
     * @param actvtyCtgr 활동 카테고리를 나타내는 `ActvtyCtgr` 객체
     */
    public LogSysParam(final Boolean rslt, final String rsltMsg, final ActvtyCtgr actvtyCtgr) {
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
        this.actvtyCtgr = actvtyCtgr;
    }

    /**
     * 시스템 로그 파라미터 객체에 결과 값과 메시지를 설정합니다.
     *
     * @param rslt 결과 값 (성공 여부)
     * @param rsltMsg 결과 메시지
     * @return {@link LogSysParam} -- 결과가 설정된 현재 객체
     */
    public LogSysParam setResult(final boolean rslt, final String rsltMsg) {
        this.rslt = rslt;
        this.rsltMsg = rsltMsg;
        return this;
    }

    /**
     * 시스템 로그 파라미터 객체에 결과 값과 메시지를 설정합니다.
     *
     * @param rslt 결과 값 (성공 여부)
     * @param rsltMsg 결과 메시지
     * @param actvtyCtgr 활동 카테고리
     * @return {@link LogSysParam} -- 결과가 설정된 현재 객체
     */
    public LogSysParam setResult(final boolean rslt, final String rsltMsg, final ActvtyCtgr actvtyCtgr) {
        this.setResult(rslt, rsltMsg);
        this.actvtyCtgr = actvtyCtgr;
        return this;
    }

    /**
     * Exception 정보를 객체에 세팅한다.
     *
     * @param e 예외 정보 객체
     */
    public void setExceptionInfo(final Exception e) {
        this.exceptionNm = MessageUtils.getExceptionNm(e);
        this.exceptionMsg = MessageUtils.getExceptionMsg(e);
    }
}
