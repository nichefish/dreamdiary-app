package io.nicheblog.dreamdiary.global.config;

import io.nicheblog.dreamdiary.auth.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig
 * <pre>
 *  웹소켓 관련 설정 Config
 * </pre>
 *
 * @author nichefish
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // "/topic"으로 시작하는 경로를 메시지 브로커에서 처리하도록 설정
        config.enableSimpleBroker("/topic");
        // 메시지 전송 시, "/app"으로 시작하는 경로로 처리하도록 설정
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 클라이언트에서 "/chat" 엔드포인트로 연결할 수 있도록 설정
        registry.addEndpoint("/chat")
                .addInterceptors(webSocketAuthInterceptor);
    }

}
