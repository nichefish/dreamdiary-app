package io.nicheblog.dreamdiary.auth.security.handler;

import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.web.util.HttpUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * AjaxAwareAuthenticationEntryPoint
 * <pre>
 *  Spring Security에서 인증 포인트 주입.
 *  AJAX 요청에는 JSON 응답을 반환하도록 설정한다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class AjaxAwareAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    /**
     * 인증되지 않은 요청이 감지되었을 때 호출되는 메서드.
     * AJAX 요청과 일반 요청을 구분하여 적절한 응답을 반환합니다.
     *
     * @param request 인증되지 않은 요청 객체 (`HttpServletRequest`)
     * @param response 인증 실패 시 응답을 처리할 객체 (`HttpServletResponse`)
     * @param authException 발생한 인증 예외 객체 (`AuthenticationException`)
     * @throws IOException 응답을 처리하는 중 입출력 오류가 발생할 경우
     */
    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException
    ) throws IOException {

        if (HttpUtils.isAjaxRequest(request)) {
            // Ajax 요청일 경우 : 에러 응답을 내려보내고, js 레벨에서 처리한다.
            // @see commons.js
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            final String detail = (authException != null && StringUtils.isNotBlank(authException.getMessage()))
                    ? authException.getMessage()
                    : "로그인이 필요하거나 인증 정보가 없습니다.";
            response.getWriter().write(String.format(Locale.ROOT,
                    "{\"error\":\"Unauthenticated\",\"message\":\"%s\"}", escapeJson(detail)));
        } else {
            // 일반 요청일 경우 : 메세지 출력 후 로그인 페이지로 리다리렉트
            response.setContentType("text/html; charset=utf-8");
            // 현재 요청 URL을 추출, 현재 URL이 로그인 페이지 URL과 다른 경우 리디렉션
            final String currentUrl = request.getRequestURI();
            if (currentUrl.equals(Url.APP_AUTH_LGN_FORM)) return;
            // alert창 띄운 후 로그인 페이지로 리다이렉트1
            final String msg = (authException != null && StringUtils.isNotBlank(authException.getMessage()))
                    ? authException.getMessage()
                    : "로그인이 필요하거나 인증 세션이 만료되었습니다. 다시 로그인해 주세요.";
            final String loginFormUrl = Url.APP_AUTH_LGN_FORM;
            try (PrintWriter out = response.getWriter()) {
                out.println("<script type=\"text/javascript\">");
                out.println("const hasSwal = (typeof Swal !== \"undefined\");");
                out.println("if (hasSwal) {");
                out.println("    Swal.fire({text: '" + escapeJsStringLiteral(msg) + "'}).then(function() {");
                out.println("        location.replace('" + loginFormUrl + "');");
                out.println("    });");
                out.println("} else {");
                out.println("    alert('" + escapeJsStringLiteral(msg) + "');");
                out.println("    location.replace('" + loginFormUrl + "');");
                out.println("}");
                out.println("</script>");
            } catch (final Exception e) {
                // 예외 발생 시 로그인 페이지로 리다이렉트
                response.sendRedirect(loginFormUrl);
            }
        }
    }

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

    /** HTML 내 <script> 단일 인용 문자열용 이스케이프 */
    private static String escapeJsStringLiteral(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ");
    }
}
