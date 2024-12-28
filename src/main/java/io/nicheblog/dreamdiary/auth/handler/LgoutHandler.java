package io.nicheblog.dreamdiary.auth.handler;

import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.service.impl.DupIdLgnManager;
import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LgoutHandler
 * <pre>
 *  Spring Security:: 로그아웃시 처리 Handler
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class LgoutHandler
        implements LogoutHandler {

    private final ApplicationEventPublisher publisher;

    /**
     * 사용자 로그아웃 처리
     *
     * @param request 로그아웃 요청 객체
     * @param httpServletResponse 응답 객체
     * @param authentication 현재 인증된 사용자의 Authentication 객체
     */
    @Override
    public void logout(
            final HttpServletRequest request,
            final HttpServletResponse httpServletResponse,
            final Authentication authentication
    ) {

        // 로그 적재
        publisher.publishEvent(new LogActvtyEvent(this, new LogActvtyParam(true)));
        // 중복 로그인 관리용 arrayList에서 로그인 아이디 제거
        if (authentication == null || authentication.getPrincipal() == null) return;
        DupIdLgnManager.removeKey(((AuthInfo) authentication.getPrincipal()).getUserId());
    }
}
