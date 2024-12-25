package io.nicheblog.dreamdiary.auth.handler;

import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.service.DupIdLgnManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SessionDestroyListener
 * <pre>
 *  중복 로그인 체크 관련 session event(destroy) listener.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class SessionDestroyListener
        implements ApplicationListener<SessionDestroyedEvent> {

    private final SimpMessagingTemplate messagingTemplate; // 메시지 전송을 위한 템플릿

    /**
     * 세션 종료 이벤트(SessionDestroyedEvent)가 발생했을 때 호출되는 메서드입니다.
     *
     * @param event 세션이 종료될 때 발생하는 SessionDestroyedEvent 객체
     */
    @Override
    public void onApplicationEvent(final SessionDestroyedEvent event) {
        final List<SecurityContext> lstSecurityContext = event.getSecurityContexts();
        for (final SecurityContext securityContext : lstSecurityContext) {
            // 중복 로그인 관리용 arrayList에서 로그인 아이디 제거
            final Authentication authentication = securityContext.getAuthentication();
            final String userId = ((AuthInfo) authentication.getPrincipal()).getUserId();
            DupIdLgnManager.removeKey(userId);

            // WebSocket 메시지로 세션 종료 알림 전송
            messagingTemplate.convertAndSend("/topic/session-invalid", "Your session has expired, please log in again.");
        }
    }
}