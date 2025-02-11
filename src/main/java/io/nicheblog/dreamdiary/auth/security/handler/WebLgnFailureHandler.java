package io.nicheblog.dreamdiary.auth.security.handler;

import io.nicheblog.dreamdiary.auth.security.exception.AccountDormantException;
import io.nicheblog.dreamdiary.auth.security.exception.AccountNeedsPwResetException;
import io.nicheblog.dreamdiary.auth.security.exception.DupIdLgnException;
import io.nicheblog.dreamdiary.auth.security.service.AuthService;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.entity.LgnPolicyEntity;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.service.LgnPolicyService;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.event.LogAnonActvtyEvent;
import io.nicheblog.dreamdiary.extension.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.handler.HttpMethodRequestWrapper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
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

/**
 * LgnFailureHandler
 * <pre>
 *  Spring Security:: 웹 로그인 실패시 처리 Handler
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class WebLgnFailureHandler
        extends SimpleUrlAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    private final AuthService authService;
    private final LgnPolicyService lgnPolicyService;
    private final ApplicationEventPublisherWrapper publisher;

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
        String errorMsg = this.getLgnFailureMsg(exception);
        /* 존재하지 않는 계정 제외하고 로그인 실패 로그 저장 */
        if (!(exception instanceof InternalAuthenticationServiceException) && !(exception instanceof DupIdLgnException)) {
            final LogActvtyParam logParam = new LogActvtyParam(userId, false, errorMsg, ActvtyCtgr.LGN);
            publisher.publishAsyncEvent(new LogAnonActvtyEvent(this, logParam));
        }
        /* 비밀번호 불일치 */
        if (exception instanceof BadCredentialsException) {
            final LgnPolicyEntity rsLgnPolicyEntity = lgnPolicyService.getDtlEntity();
            final Integer lgnTryLmt = rsLgnPolicyEntity.getLgnTryLmt();
            // 로그인 실패 횟수 처리
            final Integer newLgnFailCnt = authService.applyLgnFailCnt(userId);
            if (newLgnFailCnt < lgnTryLmt) {
                errorMsg += "<br>" + MessageUtils.getMessage(MessageUtils.LGN_FAIL_BADCREDENTIALS_CNT, new Object[]{lgnTryLmt, newLgnFailCnt});
            } else {
                authService.lockAccount(userId);
                errorMsg += "<br>" + MessageUtils.getMessage(MessageUtils.LGN_FAIL_BADCREDENTIALS_LOCKED, new Object[]{newLgnFailCnt});
            }
            // 로그인 실패 횟수 처리
        } else if (exception instanceof AccountDormantException) {
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
        } else if (exception instanceof AccountNeedsPwResetException) {
            request.setAttribute("userId", userId);
            request.setAttribute("needsPwReset", true);
        }

        log.info("login attempt failed.. userId: {} errorMsg: {}", userId, errorMsg);
        request.setAttribute(Constant.ERROR_MSG, errorMsg);
        // POST로 넘어왔던 Request 메소드를 GET으로 변경
        final HttpMethodRequestWrapper getMethodRequest = new HttpMethodRequestWrapper(request);
        // 로그인 화면으로 포워드
        request.getRequestDispatcher(Url.AUTH_LGN_FORM).forward(getMethodRequest, response);
    }

    /**
     * Spring Security Exception 이름으로 message 세팅해서 반환
     * messageBundle에 exception 클래스명으로 설정시 해당 에러메세지 반환
     *
     * @param e 예외 객체
     * @return {@link String} -- 예외에 해당하는 에러 메시지
     */
    public String getLgnFailureMsg(final Exception e) {
        String exceptionNm = e.getClass().toString();
        exceptionNm = exceptionNm.substring(exceptionNm.lastIndexOf('.') + 1);
        return MessageUtils.getMessage("AbstractUserDetailsAuthenticationProvider." + exceptionNm);
    }
}