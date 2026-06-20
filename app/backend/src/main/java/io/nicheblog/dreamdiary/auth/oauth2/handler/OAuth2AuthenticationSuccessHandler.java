package io.nicheblog.dreamdiary.auth.oauth2.handler;

import io.nicheblog.dreamdiary.auth.oauth2.provider.OAuth2Provider;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.service.AuthService;
import io.nicheblog.dreamdiary.auth.security.service.AuthSessionPolicyService;
import io.nicheblog.dreamdiary.auth.security.service.manager.DupIdLoginManager;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.handler.LogEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import io.nicheblog.dreamdiary.global.Url;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * OAuth2AuthenticationSuccessHandler
 * <pre>
 *  OAuth2 소셜 인증 성공시 바로 SuccessHandler를 탄다.
 *  "로그인 후 이전 페이지 이동" 기능 구현 위해 SavedRequestAwareAuthenticationSuccessHandler 상속
 *  소셜 로그인 성공시 바로 SuccessHandler 를 탄다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2AuthenticationSuccessHandler
        extends SavedRequestAwareAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final OAuth2Provider oAuth2provider;
    private final AuthService authService;
    private final AuthSessionPolicyService authSessionPolicyService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * OAuth2 인증 성공 시 처리하는 메소드.
     * 로그인 성공 후 세션 초기화 및 로그인 기록을 남기고 이전 페이지로 리다이렉트합니다.
     *
     * @param request 로그인 요청 객체
     * @param response 응답을  객체
     * @param authentication 인증된 사용자 정보를 담은 {@link Authentication} 객체
     * @throws IOException 입출력 예외 발생 시
     * @see LogEventListener
     */
    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) throws IOException {

        // 인증 처리
        final OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        try {
            final AuthInfo authInfo = oAuth2provider.authenticate(oauthToken);

            // // 사용자 정보 세션에 추가
            final ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            final HttpSession session = servletRequestAttribute.getRequest().getSession();
            authSessionPolicyService.applyToSession(session);
            session.setAttribute("authInfo", authInfo);
            session.setAttribute("remoteIp", AuthUtils.getRemoteIpAddr());

            // 최종 로그인 날짜 세팅 및 패스워드오류 카운트 초기화
            final String username = authInfo.getUsername();
            authService.setLastLoginAt(username);
            // session에 loginId attribute 추가 :: 중복 로그인 방지 비교용
            DupIdLoginManager.addKey(username);

            // 로그인 로그 남기기
            final LogParam logParam = new LogParam(true, MessageUtils.RSLT_SUCCESS, ActvtyCtgr.LGN);
            publisher.publishAsyncEvent(new LogEvent(this, logParam));

            // 로그인 성공시 브라우저 캐시 초기화 처리
            HttpUtils.setInvalidateBrowserCacheHeader(response);

            // 세션에서 인증 관련 임시 속성 제거
            clearAuthenticationAttributes(request);

            // 로그인 성공 스크립트 처리 :: response 커밋 후 super 를 호출하지 않는다.
            this.setSuccessResponse(response);

            // 로그인 성공 로그 처리
            publisher.publishAsyncEvent(new LogEvent(this, new LogParam(true)));
        } catch (Exception e) {
            // 로그인 실패 스크립트 처리
            final String errorMsg = e.getMessage();
            this.setFaiilureResponse(response, errorMsg);
            
            // 로그인 실패 로그 처리
            publisher.publishAsyncEvent(new LogEvent(this, new LogParam(true)));
        }
    }

    /**
     * Response에 Javascript alert 처리 및 리다이렉트
     *
     * @param response HTTP 응답 객체
     * @throws IOException 응답에 문제가 발생할 경우
     */
    public void setSuccessResponse(final HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        // Vue SPA 팝업 인증 성공 :: opener 를 메인으로 이동시키고 팝업을 닫는다.
        final String url = "/";
        try (final PrintWriter out = response.getWriter()) {
            out.println("<script>");
            out.println("if (window.opener) { window.opener.location.replace('" + url + "'); }");
            out.println("window.close();");
            out.println("</script>");
        } catch (final IOException e) {
            log.info(MessageUtils.getExceptionMsg(e));
            response.sendRedirect(url);
        }
    }

    /**
     * Response에 Javascript alert 처리 및 리다이렉트
     *
     * @throws IOException 응답에 문제가 발생할 경우
     */
    public void setFaiilureResponse(final HttpServletResponse response, final String errorMsg) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        // Vue SPA 팝업 인증 실패 :: 오류 메시지를 쿼리 파라미터로 Vue 로그인 화면에 전달하고 팝업을 닫는다.
        final String loginUrl = Url.VUE_SIGN_IN;
        final String safeMsg = (errorMsg != null) ? errorMsg.replace("'", "\\'") : "";
        try (final PrintWriter out = response.getWriter()) {
            out.println("<script>");
            out.println("const errMsg = encodeURIComponent('" + safeMsg + "');");
            out.println("if (window.opener) { window.opener.location.replace('" + loginUrl + "?oauthError=' + errMsg); }");
            out.println("window.close();");
            out.println("</script>");
        } catch (final IOException e) {
            log.info(MessageUtils.getExceptionMsg(e));
            response.sendRedirect(loginUrl);
        }
    }
}
