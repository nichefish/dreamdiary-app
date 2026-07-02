package io.nicheblog.dreamdiary.auth.security.matcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketHandshakeRequestMatcherTest {

    private WebSocketHandshakeRequestMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new WebSocketHandshakeRequestMatcher();
    }

    @Test
    void allowsOnlyGetChatHandshakeRequest() {
        assertThat(matches("GET", "/chat")).isTrue();
        assertThat(matches("POST", "/chat")).isFalse();
    }

    @Test
    void rejectsChatRestRequests() {
        assertThat(matches("GET", "/chat/settings")).isFalse();
        assertThat(matches("GET", "/chat/sessions")).isFalse();
        assertThat(matches("GET", "/chat/sessions/1/messages")).isFalse();
    }

    private boolean matches(final String method, final String path) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return matcher.matches(request);
    }
}
