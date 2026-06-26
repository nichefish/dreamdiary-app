package io.nicheblog.dreamdiary.auth.security.matcher;

import io.nicheblog.dreamdiary.global.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PublicApiRequestMatcherTest {

    private PublicApiRequestMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new PublicApiRequestMatcher();
    }

    @Test
    void allowsLoginAndRefreshPostRequests() {
        assertThat(matches("POST", Url.API_AUTH_LOGIN)).isTrue();
        assertThat(matches("POST", Url.API_AUTH_REFRESH)).isTrue();
    }

    @Test
    void allowsVerificationAndDuplicateCheckGetRequests() {
        assertThat(matches("GET", "/api/auth/verify/token-value")).isTrue();
        assertThat(matches("GET", Url.USERS_DUPLICATE_USERNAME_CHECK)).isTrue();
        assertThat(matches("GET", Url.USERS_DUPLICATE_EMAIL_CHECK)).isTrue();
    }

    @Test
    void allowsOnlyPostForSignupRequests() {
        assertThat(matches("POST", Url.USER_SIGNUP_REQUESTS)).isTrue();
        assertThat(matches("GET", Url.USER_SIGNUP_REQUESTS)).isFalse();
    }

    @Test
    void rejectsProtectedAuthAndJournalApis() {
        assertThat(matches("GET", Url.API_AUTH_INFO)).isFalse();
        assertThat(matches("GET", Url.API_SESSION_PING)).isFalse();
        assertThat(matches("GET", "/api/journal/days")).isFalse();
    }

    private boolean matches(final String method, final String path) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return matcher.matches(request);
    }
}
