package io.nicheblog.dreamdiary.auth.handler;

import io.nicheblog.dreamdiary.auth.exception.AcntDormantException;
import io.nicheblog.dreamdiary.auth.exception.AcntNeedsPwResetException;
import io.nicheblog.dreamdiary.auth.exception.DupIdLgnException;
import io.nicheblog.dreamdiary.auth.provider.OAuth2Provider;
import io.nicheblog.dreamdiary.auth.service.AuthService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogAnonActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * OAuth2AuthenticationFailureHandler
 * <pre>
 *  OAuth2 소셜 인증 실패 처리 핸들러
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2AuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    private final OAuth2Provider oAuth2provider;
    private final AuthService authService;
    private final ApplicationEventPublisher publisher;

    /**
     * 웹 로그인 로그인 실패시 상황별 분기 처리
     *
     * @param request 로그인 요청 객체
     * @param response 응답 객체
     * @param exception 인증 실패 예외 객체 {@link AuthenticationException}
     * @see LogActvtyEventListener
     */
    @SneakyThrows
    @Override
    public void onAuthenticationFailure(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AuthenticationException exception
    ) {

        request.removeAttribute("userId");
        request.removeAttribute("needsPwReset");
        final String userId = request.getParameter("userId");
        final String errorMsg = exception.getMessage();
        /* 존재하지 않는 계정 제외하고 로그인 실패 로그 저장 */
        if (!(exception instanceof InternalAuthenticationServiceException) && !(exception instanceof DupIdLgnException)) {
            final LogActvtyParam logParam = new LogActvtyParam(userId, false, errorMsg, ActvtyCtgr.LGN);
            publisher.publishEvent(new LogAnonActvtyEvent(this, logParam));
        }
        /* 비밀번호 불일치 */
        if (exception instanceof AcntDormantException) {
            authService.lockAccount(userId);        // 계정 잠금 처리
            /* 비밀번호 변경기간 만료 */
        } else if (exception instanceof CredentialsExpiredException) {
            request.setAttribute("userId", userId);
            request.setAttribute("isCredentialExpired", true);
            /* 중복 로그인 방지 */
        } else if (exception instanceof DupIdLgnException) {
            request.setAttribute("userId", userId);
            // 세션에서 중복 아이디 정보 관리
            final ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            final HttpSession session = servletRequestAttribute.getRequest().getSession();
            session.setAttribute("isDupIdLgn", userId);
            /* 패스워드 초기화 강제 */
        } else if (exception instanceof AcntNeedsPwResetException) {
            request.setAttribute("userId", userId);
            request.setAttribute("needsPwReset", true);
        }

        log.info("login attempt failed.. userId: {} errorMsg: {}", userId, errorMsg);
        request.setAttribute(Constant.ERROR_MSG, errorMsg);

        this.setFaiilureResponse(response, errorMsg);
    }

    /**
     * Response에 Javascript alert 처리 및 리다이렉트
     *
     * @throws IOException 응답에 문제가 발생할 경우
     */
    public void setFaiilureResponse(final HttpServletResponse response, final String errorMsg) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        try (final PrintWriter out = response.getWriter()) {
            out.println("<script language=\"JavaScript\" type=\"text/JavaScript\">");
            out.println("const hasSwal = (typeof Swal !== \"undefined\");");
            out.println("if (hasSwal) {");
            out.println("   Swal.fire({\"text\": `" + errorMsg + "`}).then(() => { window.close(); });");
            out.println("} else {");
            out.println("   alert(\"" + errorMsg + "\");");
            out.println("   window.close();");
            out.println("}");
            out.println("</script>");
        } catch (final IOException e) {
            log.info(MessageUtils.getExceptionMsg(e));
            response.sendRedirect("/");
        }
    }
}
