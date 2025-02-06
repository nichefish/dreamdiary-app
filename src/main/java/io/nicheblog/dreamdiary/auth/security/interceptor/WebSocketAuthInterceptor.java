package io.nicheblog.dreamdiary.auth.security.interceptor;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
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

    /**
     * WebSocket 핸드셰이크 요청 전에 실행.
     * JWT 또는 Principal을 이용해 인증 정보를 설정합니다.
     */
    @Override
    public boolean beforeHandshake(
            final @NotNull ServerHttpRequest request,
            final @NotNull ServerHttpResponse response,
            final @NotNull WebSocketHandler wsHandler,
            final @NotNull Map<String, Object> attributes) throws Exception {

        final String token = jwtTokenProvider.resolveToken(request);  // 토큰 가져오기
        if (token != null) {
            final Authentication authentication = jwtTokenProvider.getDirectAuthentication(token);
            attributes.put("authentication", authentication);  // 웹소켓 세션에 인증 정보 추가
        } else {
            final Principal authentication = request.getPrincipal();
            attributes.put("authentication", authentication);  // 웹소켓 세션에 인증 정보 추가
        }
        // 인증 부재시 웹소켓 연결 거부
        return (attributes.get("authentication") != null);
    }


    /**
     * WebSocket 핸드셰이크 요청 ㅎ에 실행.
     */
    @Override
    public void afterHandshake(
            final @NotNull ServerHttpRequest request,
            final @NotNull ServerHttpResponse response,
            final @NotNull WebSocketHandler wsHandler,
            final Exception exception) {
        //
    }
}