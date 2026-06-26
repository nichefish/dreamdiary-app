package io.nicheblog.dreamdiary.auth.security.handler;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionDestroyListenerTest {

    @Test
    void sendsSessionInvalidNotificationOnlyToDestroyedSessionUser() {
        final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        final SessionDestroyListener listener = new SessionDestroyListener(messagingTemplate);
        final SessionDestroyedEvent event = mock(SessionDestroyedEvent.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        final Authentication authentication = mock(Authentication.class);
        final AuthInfo authInfo = mock(AuthInfo.class);

        when(event.getSecurityContexts()).thenReturn(List.of(securityContext));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(authInfo);
        when(authInfo.getUsername()).thenReturn("target-user");

        listener.onApplicationEvent(event);

        verify(messagingTemplate).convertAndSendToUser(
                "target-user",
                "/queue/session-invalid",
                "Your session has expired, please log in again."
        );
    }
}
