package io.nicheblog.dreamdiary.auth.security.matcher;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/** WebSocket 인증 인터셉터가 처리하는 채팅 핸드셰이크 요청만 식별한다. */
@Component
public class WebSocketHandshakeRequestMatcher implements RequestMatcher {

    private final RequestMatcher delegate = new AntPathRequestMatcher("/chat", "GET");

    /**
     * 요청이 채팅 WebSocket 핸드셰이크 경로와 HTTP 메서드에 일치하는지 확인한다.
     *
     * @param request HTTP 요청
     * @return 채팅 WebSocket 핸드셰이크 요청이면 true
     */
    @Override
    public boolean matches(final HttpServletRequest request) {
        return delegate.matches(request);
    }
}
