package io.nicheblog.dreamdiary.auth.security.interceptor;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
 *  WebSocket Handshake 인증 처리를 수행한다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * WebSocket 핸드셰이크 요청 전에 실행.
     * JWT 또는 Principal을 이용해 인증 정보를 설정한다.
     */
    @Override
    public boolean beforeHandshake(
            final @NotNull ServerHttpRequest request,
            final @NotNull ServerHttpResponse response,
            final @NotNull WebSocketHandler wsHandler,
            final @NotNull Map<String, Object> attributes) {

        try {
            final String token = jwtTokenProvider.resolveToken(request);
            if (token != null) {
                final Authentication authentication = jwtTokenProvider.getDirectAuthentication(token);
                attributes.put("authentication", authentication);
            } else {
                final Principal authentication = request.getPrincipal();
                attributes.put("authentication", authentication);
            }
            return attributes.get("authentication") != null;
        } catch (final Exception e) {
            log.warn("WebSocket authentication rejected: {}", e.getMessage());
            attributes.remove("authentication");
            return false;
        }
    }

    /**
     * WebSocket 핸드셰이크 요청 후에 실행.
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
