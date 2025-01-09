package io.nicheblog.dreamdiary.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * MessageUtil
 * <pre>
 *  메세지 처리 유틸리티 모듈.
 *  "Spring Boot에서는 src/main/resources/messages.properties를 찾았을 때 자동으로 MessageSource 빈을 등록한다."
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class MessageUtils
        extends ReloadableResourceBundleMessageSource
        implements MessageSource {

    private final MessageSource autowiredMessageSource;
    private final HttpServletResponse autowiredResponse;

    private static MessageSource messageSource;
    private static HttpServletResponse response;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        messageSource = autowiredMessageSource;
        response = autowiredResponse;
    }

    public static final String RSLT_SUCCESS = "common.rslt.success";
    public static final String RSLT_FAILURE = "common.rslt.failure";
    public static final String RSLT_EXCEPTION = "common.rslt.exception";
    public static final String RSLT_EMPTY = "common.rslt.empty";

    public static final String INVALID_TOKEN = "common.rslt.exception";

    public static final String RSLT_JANDI_SUCCESS = "commons.rslt.jandi.success";
    public static final String RSLT_JANDI_FAILURE = "commons.rslt.jandi.failure";

    public static final String RSLT_USER_ID_DUP = "userInfo.ipDupChck.rslt.idUnusable";
    public static final String RSLT_USER_ID_NOT_DUP = "userInfo.ipDupChck.rslt.idUsable";
    public static final String RSLT_SUCCESS_PW_RESET = "userInfo.resetPw.rslt.success";

    public static final String LGN_FAIL_BADCREDENTIALS_CNT = "AbstractUserDetailsAuthenticationProvider.badCredentials.failCnt";
    public static final String LGN_FAIL_BADCREDENTIALS_LOCKED = "AbstractUserDetailsAuthenticationProvider.badCredentials.locked";

    public static final String NOT_DELABLE_OWN_ID = "본인 아이디는 삭제할 수 없습니다.";
    public static final String PW_MISMATCH = "비밀번호가 일치하지 않습니다.";

    /**
     * 코드로 사전 정의된 메세지 조회
     *
     * @param code 메시지 코드
     * @return {@link String} -- 해당 코드에 해당하는 메시지
     * @throws NoSuchMessageException 메시지가 존재하지 않는 경우 발생
     */
    public static String getMessage(final String code) throws NoSuchMessageException {
        // test환경에서의 난해성 때문에 bean 주입 환경 외에는 예외 리턴 처리
        if (messageSource == null) return null;
        String msg = messageSource.getMessage(code, null, Locale.getDefault());
        log.info("code: {}, msg: {}", code, msg);
        return msg;
    }

    /**
     * 코드로 사전 정의된 메세지 조회
     *
     * @param code 메시지 코드
     * @param args 메시지 내 파라미터
     * @return {@link String} -- 해당 코드와 파라미터에 맞는 메시지
     * @throws NoSuchMessageException 메시지가 존재하지 않는 경우 발생
     */
    public static String getMessage(final String code, final @Nullable Object[] args) throws NoSuchMessageException {
        String msg = messageSource.getMessage(code, args, Locale.getDefault());
        log.info("code: {}, msg: {}", code, msg);
        return msg;
    }

    /**
     * Javascript로 alert 처리
     *
     * @param msg 화면에 표시할 메시지
     * @throws IOException 응답에 문제가 발생할 경우
     */
    public static void alertMessage(final String msg) throws IOException {
        alertMessage(msg, null);
    }

    /**
     * Response에 Javascript alert 처리 및 리다이렉트
     *
     * @param msg 화면에 표시할 메시지
     * @param url 리다이렉트할 URL (null 가능)
     * @throws IOException 응답에 문제가 발생할 경우
     */
    public static void alertMessage(final String msg, final String url) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<script language=\"JavaScript\" type=\"text/JavaScript\">");
            out.println("const hasSwal = (typeof Swal !== \"undefined\");");
            if (url != null) {
                out.println("if (hasSwal) { Swal.fire({\"text\": `" + msg + "`}).then(location.replace('" + url + "')); }");
                out.println("else { alert(`" + msg + "`); location.replace('" + url + "'); }");
            } else {
                out.println("if (hasSwal) { Swal.fire({\"text\": `" + msg + "`}); }");
                out.println("else { alert(`" + msg + "`); }");
            }
            out.println("</script>");
        } catch (IOException e) {
            log.info(getExceptionMsg(e));
            response.sendRedirect("/");
        }
    }

    /**
     * 공통 > Exception 클래스를 받아서 해당 message를 세팅해서 반환
     * messageBundle에 exception 클래스명으로 설정시 해당 에러메세지를 반환한다.
     *
     * @param e 발생한 예외
     * @return {@link String} -- 예외 메시지
     */
    public static String getExceptionMsg(final Throwable e) {
        if (StringUtils.isNotEmpty(e.getMessage())) return e.getMessage();
        String exceptionNm = getExceptionNm(e);
        String rsltMsg = getMessage(RSLT_EXCEPTION + "." + exceptionNm);
        log.error("exceptionNm: {}, rsltMsg: {}. {}", exceptionNm, rsltMsg, e.getStackTrace());
        return rsltMsg;
    }

    /**
     * 공통 > Exception 클래스를 받아서 해당 message를 세팅해서 반환
     *
     * @param e 발생한 예외
     * @return {@link String} -- 예외 메시지
     */
    public static String getExceptionNm(final Throwable e) {
        String exceptionNm = e.getClass()
                              .toString();
        return exceptionNm.substring(exceptionNm.lastIndexOf('.') + 1);
    }

    /**
     * MessageSource의 메시지를 Map으로 반환
     *
     * @return Map<String, String> -- messageMap
     */
    public static Object getMessageMap() {
        final ResourceBundle bundle = ResourceBundle.getBundle("messages/messages", Locale.getDefault());
        final Map<String, String> messageMap = new HashMap<>();
        bundle.keySet().forEach(key -> messageMap.put(key, bundle.getString(key)));

        return messageMap;
    }

}