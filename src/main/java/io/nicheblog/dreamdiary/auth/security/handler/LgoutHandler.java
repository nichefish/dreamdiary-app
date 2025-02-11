package io.nicheblog.dreamdiary.auth.security.handler;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.service.manager.DupIdLgnManager;
import io.nicheblog.dreamdiary.extension.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.extension.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
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

    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 사용자 로그아웃 처리
     *
     * @param request 로그아웃 요청 객체
     * @param httpServletResponse 응답 객체
     * @param authentication 현재 인증된 사용자의 Authentication 객체
     * @see LogActvtyEventListener
     */
    @Override
    public void logout(
            final HttpServletRequest request,
            final HttpServletResponse httpServletResponse,
            final Authentication authentication
    ) {

        // 로그 적재
        publisher.publishAsyncEvent(new LogActvtyEvent(this, new LogActvtyParam(true)));
        // 중복 로그인 관리용 arrayList에서 로그인 아이디 제거
        if (authentication == null || authentication.getPrincipal() == null) return;

        DupIdLgnManager.removeKey(((AuthInfo) authentication.getPrincipal()).getUserId());

        // 쿠키에서 JWT 토큰 삭제
        removeJwtFromCookie(httpServletResponse);
    }

    /**
     * 쿠키에서 JWT 토큰 삭제
     *
     * @param response 응답 객체
     */
    private void removeJwtFromCookie(final HttpServletResponse response) {
        // JWT를 저장한 쿠키 이름이 "jwtToken"이라고 가정
        // 쿠키 만료 시간을 0으로 설정하여 삭제 처리
        final Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);  // 클라이언트에서 JavaScript 접근 불가
        // cookie.setSecure(true);    // HTTPS 연결에서만 전달
        cookie.setPath("/");       // 전체 경로에서 유효
        cookie.setMaxAge(0);       // 만료 시간 0으로 설정

        response.addCookie(cookie); // 응답에 쿠키 추가
    }
}
