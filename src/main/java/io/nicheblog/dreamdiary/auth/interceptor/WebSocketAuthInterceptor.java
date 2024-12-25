package io.nicheblog.dreamdiary.auth.interceptor;

import io.nicheblog.dreamdiary.auth.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocketAuthInterceptor
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

        // TODO: 연결 만들 때부터 헤더에 토큰 넘겨서 검증하기
        // if (token != null && jwtTokenProvider.validateToken(token)) {
        //     Authentication authentication = jwtTokenProvider.getAuthentication(token);  // 인증 정보 추출
        //     attributes.put("authentication", authentication);  // 웹소켓 세션에 인증 정보 추가
        // } else {
        //     return false;  // 토큰이 없거나 유효하지 않으면 인증 실패 처리
        // }

        return true;
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