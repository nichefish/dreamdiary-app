package io.nicheblog.dreamdiary.auth.interceptor;

import io.nicheblog.dreamdiary.auth.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocketAuthInterceptor
 * <pre>
 *  웹소켓 Handshake시 인증 절차를 수행한다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(
            @NotNull ServerHttpRequest request,
            @NotNull ServerHttpResponse response,
            @NotNull WebSocketHandler wsHandler,
            @NotNull Map<String, Object> attributes) throws Exception {

        String token = jwtTokenProvider.resolveToken(request);  // 토큰 가져오기
        if (token != null) {
            Authentication authentication = jwtTokenProvider.getDirectAuthentication(token);
            attributes.put("authentication", authentication);  // 웹소켓 세션에 인증 정보 추가
        } else {
            Principal authentication = request.getPrincipal();
            attributes.put("authentication", authentication);  // 웹소켓 세션에 인증 정보 추가
        }
        // 인증 부재시 웹소켓 연결 거부
        return (attributes.get("authentication") != null);
    }

    @Override
    public void afterHandshake(
            @NotNull ServerHttpRequest request,
            @NotNull ServerHttpResponse response,
            @NotNull WebSocketHandler wsHandler,
            Exception exception) {
        //
    }
}