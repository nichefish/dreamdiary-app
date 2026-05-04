package io.nicheblog.dreamdiary.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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

    @Resource(name = "messageSource")
    private MessageSource autowiredMessageSource;
    private final HttpServletResponse autowiredResponse;

    private static MessageSource messageSource;
    private static HttpServletResponse response;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        messageSource = autowiredMessageSource;
        response = autowiredResponse;
        RSLT_SUCCESS = getMessage("msg.rslt.success");
        RSLT_FAILURE = getMessage("msg.rslt.failure");
        RSLT_EMPTY = getMessage("msg.rslt.empty");
    }

    public static String RSLT_SUCCESS;
    public static String RSLT_FAILURE;
    public static String RSLT_EMPTY;

    public static final String RSLT_EXCEPTION = "exception";

    public static final String RSLT_JANDI_SUCCESS = "msg.jandi.rslt.success";
    public static final String RSLT_JANDI_FAILURE = "msg.jandi.rslt.failure";

    public static final String RSLT_SUCCESS_PW_RESET = "msg.user.pw.reset.rslt.success";

    public static final String LGN_FAIL_BADCREDENTIALS_CNT = "AbstractUserDetailsAuthenticationProvider.BadCredentials.failCnt";
    public static final String LGN_FAIL_BADCREDENTIALS_LOCKED = "AbstractUserDetailsAuthenticationProvider.BadCredentials.locked";

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
        return messageSource.getMessage(code, null, code, Locale.getDefault());
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
        if (messageSource == null) return code;
        return messageSource.getMessage(code, args, code, Locale.getDefault());
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
     * Javascript로 message key에 해당하는 alert 처리
     *
     * @param key 메시지 키
     * @throws IOException 응답에 문제가 발생할 경우
     */
    public static void alertMessageByKey(final String key) throws IOException {
        alertMessageByKey(key, null);
    }

    /**
     * Response에 message key에 해당하는 Javascript alert 처리 및 리다이렉트
     *
     * @param key 메시지 키
     * @param url 리다이렉트할 URL (null 가능)
     * @throws IOException 응답에 문제가 발생할 경우
     */
    public static void alertMessageByKey(final String key, final String url) throws IOException {
        alertMessage(getMessage(key), url);
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
        try (final PrintWriter out = response.getWriter()) {
            out.println("<script language=\"JavaScript\" type=\"text/JavaScript\">");
            out.println("const hasSwal = (typeof Swal !== \"undefined\");");
            if (url != null) {
                out.println("if (hasSwal) { ");
                out.println("   Swal.fire({\"text\": `" + msg + "`}).then(location.replace('" + url + "'));");
                out.println("} else { ");
                out.println("   alert(`" + msg + "`); ");
                out.println("   location.replace('" + url + "');");
                out.println("}");
            } else {
                out.println("if (hasSwal) {");
                out.println("   Swal.fire({\"text\": `" + msg + "`});");
                out.println("} else {");
                out.println("   alert(`" + msg + "`);");
                out.println("}");
            }
            out.println("</script>");
        } catch (final IOException e) {
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
        final String msg = StringUtils.trimToNull(e.getMessage());
        if (msg != null) {
            final String resolvedMsg = getMessage(msg);
            if (!msg.equals(resolvedMsg)) return resolvedMsg;
            final String exceptionMsg = getExceptionBundleMsg(e);
            if (StringUtils.isAsciiPrintable(msg) && exceptionMsg != null) return exceptionMsg;
            if (msg.length() > 200) return msg.substring(0, 200) + "...";
            return msg;
        }

        final String exceptionMsg = getExceptionBundleMsg(e);
        return exceptionMsg != null ? exceptionMsg : getMessage("msg.rslt.exception");
    }

    /**
     * 예외 클래스명에 대응되는 메시지 번들 값을 조회한다.
     * 외부 라이브러리의 기본 영어 메시지가 사용자 화면에 노출되지 않도록 하는 보조 경로다.
     *
     * @param e 발생한 예외
     * @return 메시지 번들에 등록된 예외 메시지, 없으면 null
     */
    private static String getExceptionBundleMsg(final Throwable e) {
        final String exceptionNm = getExceptionNm(e);
        final String bundleKey = RSLT_EXCEPTION + "." + exceptionNm;
        final String rsltMsg = getMessage(bundleKey);
        return bundleKey.equals(rsltMsg) ? null : rsltMsg;
    }

    /**
     * 공통 > Exception 이름으로 해당 message 반환
     * messageBundle에 exception 클래스명으로 설정시 해당 에러메세지를 반환한다.
     *
     * @param exceptionNm 발생한 예외 이름 ("Exception" 제외)
     * @return {@link String} -- 예외 메시지
     */
    public static String getExceptionMsg(final String exceptionNm) {
        return getMessage(RSLT_EXCEPTION + "." + exceptionNm);
    }

    /**
     * 공통 > Exception 클래스를 받아서 해당 message를 세팅해서 반환
     *
     * @param e 발생한 예외
     * @return {@link String} -- 예외 메시지
     */
    public static String getExceptionNm(final Throwable e) {
        final String exceptionNm = e.getClass().toString();
        return exceptionNm.substring(exceptionNm.lastIndexOf('.') + 1);
    }

    /**
     * MessageSource의 메시지를 Map으로 반환
     *
     * @return {@link Map} -- messageMap
     */
    public static Object getMessageMap() {
        final ResourceBundle bundle = ResourceBundle.getBundle("messages/messages", Locale.getDefault());
        final Map<String, String> messageMap = new HashMap<>();
        bundle.keySet().forEach(key -> messageMap.put(key, bundle.getString(key)));

        return messageMap;
    }

}
