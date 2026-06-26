package io.nicheblog.dreamdiary.auth.security.handler;

import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.util.HttpUtils;
import lombok.extern.log4j.Log4j2;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Spring Security에서 인증이 필요한 요청을 가로채는 진입점.
 * AJAX 요청에는 JSON 응답을, 일반 요청에는 로그인 페이지 이동 스크립트를 반환한다.
 *
 * @author nichefish
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class AjaxAwareAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    /**
     * 인증되지 않은 요청을 감지했을 때 호출되는 메서드.
     * Spring Security 기본 영어 메시지가 화면에 새지 않도록 서비스 메시지 키를 사용한다.
     *
     * @param request 인증되지 않은 요청 객체
     * @param response 인증 실패 응답을 처리할 객체
     * @param authException 발생한 인증 예외 객체
     * @throws IOException 응답 처리 중 입출력 오류가 발생한 경우
     */
    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException
    ) throws IOException {

        final String loginRequiredMessage = MessageUtils.getMessage("msg.auth.login-required");
        if (HttpUtils.isAjaxRequest(request)) {
            // Ajax 요청은 클라이언트 공통 핸들러에서 처리할 수 있도록 JSON으로 내려보낸다.
            // @see commons.js
            securityErrorResponseWriter.write(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    loginRequiredMessage
            );
            return;
        }

        // 일반 요청은 안내 후 로그인 페이지로 보낸다.
        response.setContentType("text/html; charset=utf-8");
        final String currentUrl = request.getRequestURI();
        if (currentUrl.equals(Url.VUE_SIGN_IN)) return;

        final String loginFormUrl = Url.VUE_SIGN_IN;
        try (PrintWriter out = response.getWriter()) {
            out.println("<script type=\"text/javascript\">");
            out.println("const isPopup = !!window.opener && window.opener !== window;");
            out.println("const hasSwal = (typeof Swal !== \"undefined\");");
            out.println("if (isPopup) {");
            out.println("    try { window.opener.location.href = '" + loginFormUrl + "'; } catch (e) {}");
            out.println("    alert('" + escapeJsStringLiteral(loginRequiredMessage) + "');");
            out.println("    window.close();");
            out.println("} else if (hasSwal) {");
            out.println("    Swal.fire({text: '" + escapeJsStringLiteral(loginRequiredMessage) + "'}).then(function() {");
            out.println("        location.replace('" + loginFormUrl + "');");
            out.println("    });");
            out.println("} else {");
            out.println("    alert('" + escapeJsStringLiteral(loginRequiredMessage) + "');");
            out.println("    location.replace('" + loginFormUrl + "');");
            out.println("}");
            out.println("</script>");
        } catch (final Exception e) {
            // 스크립트 응답 생성에 실패하면 최소한 로그인 페이지로 이동시킨다.
            response.sendRedirect(loginFormUrl);
        }
    }

    /**
     * JSON 문자열 값으로 내려보낼 수 있도록 특수문자를 이스케이프한다.
     *
     * @param s 이스케이프할 문자열
     * @return JSON 문자열 값에 안전하게 넣을 수 있는 문자열
     */
    private static String escapeJson(final String s) {
        if (s == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        sb.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * HTML script 문자열 리터럴에 넣을 수 있도록 특수문자를 이스케이프한다.
     *
     * @param s 이스케이프할 문자열
     * @return script 문자열 리터럴에 안전하게 넣을 수 있는 문자열
     */
    private static String escapeJsStringLiteral(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ");
    }
}
