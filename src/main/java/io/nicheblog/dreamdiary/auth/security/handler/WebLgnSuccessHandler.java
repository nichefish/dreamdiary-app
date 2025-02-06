package io.nicheblog.dreamdiary.auth.security.handler;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.service.AuthService;
import io.nicheblog.dreamdiary.auth.security.service.manager.DupIdLgnManager;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.util.HttpUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LgnSuccessHandler
 * <pre>
 *  Spring Security:: 웹로그인 성공시 처리 Handler
 *  "로그인 후 이전 페이지 이동" 기능 구현 위해 SavedRequestAwareAuthenticationSuccessHandler 상속
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class WebLgnSuccessHandler
        extends SavedRequestAwareAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ApplicationEventPublisher publisher;
    private final HttpSession session;

    /**
     * 웹 로그인 인증 성공 시 처리하는 메소드.
     * 로그인 성공 후 세션 초기화 및 로그인 기록을 남기고 이전 페이지로 리다이렉트합니다.
     *
     * @param request 로그인 요청 객체
     * @param response 응답을  객체
     * @param authentication 인증된 사용자 정보를 담은 {@link Authentication} 객체
     * @throws IOException 입출력 예외 발생 시
     * @throws ServletException 서블릿 예외 발생 시
     * @see LogActvtyEventListener
     */
    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) throws IOException, ServletException {

        request.removeAttribute(Constant.ERROR_MSG);
        request.removeAttribute("isCredentialExpired");
        request.removeAttribute("isDupIdLgn");
        request.removeAttribute("needsPwReset");

        // 사용자 정보 세션에 추가
        final AuthInfo authInfo = (AuthInfo) authentication.getPrincipal();
        authInfo.nullifyPasswordInfo();
        session.setAttribute("authInfo", authInfo);
        session.setAttribute("acsIp", AuthUtils.getAcsIpAddr());

        // 최종 로그인 날짜 세팅 및 패스워드오류 카운트 초기화
        final String userId = authInfo.getUserId();
        authService.setLstLgnDt(userId);
        // session에 lgnId attribute 추가 :: 중복 로그인 방지 비교용
        DupIdLgnManager.addKey(userId);

        // 로그인 로그 남기기
        final LogActvtyParam logParam = new LogActvtyParam(true, MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS), ActvtyCtgr.LGN);
        publisher.publishEvent(new LogActvtyEvent(this, logParam));

        // 로그인 성공시 브라우저 캐시 초기화 처리
        HttpUtils.setInvalidateBrowserCacheHeader(response);

        // 이전 페이지 :: 부재시 메인 페이지로 리다이렉트
        // 상속받은 상위 SavedRequestAwareAuthenticationSuccessHandler의 메소드 call
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
