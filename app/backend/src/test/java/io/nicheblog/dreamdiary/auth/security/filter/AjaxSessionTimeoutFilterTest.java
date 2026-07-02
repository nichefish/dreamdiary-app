package io.nicheblog.dreamdiary.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.auth.security.handler.SecurityErrorResponseWriter;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import javax.servlet.FilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class AjaxSessionTimeoutFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AjaxSessionTimeoutFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AjaxSessionTimeoutFilter(
                mock(ApplicationEventPublisher.class),
                new SecurityErrorResponseWriter(objectMapper)
        );
    }

    @Test
    void ajaxAuthenticationExceptionReturnsStructuredUnauthorizedResponse() throws Exception {
        final MockHttpServletRequest request = ajaxRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain chain = mock(FilterChain.class);
        final InsufficientAuthenticationException exception = new InsufficientAuthenticationException("auth detail");
        doThrow(exception).when(chain).doFilter(request, response);

        try (MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("msg.auth.login-required")).thenReturn("login required");
            messages.when(() -> MessageUtils.getExceptionMsg(exception)).thenReturn("auth detail");
            filter.doFilter(request, response, chain);
        }

        assertResponse(response, 401, "login required");
    }

    @Test
    void ajaxAccessDeniedExceptionReturnsStructuredForbiddenResponse() throws Exception {
        final MockHttpServletRequest request = ajaxRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain chain = mock(FilterChain.class);
        final AccessDeniedException exception = new AccessDeniedException("denied");
        doThrow(exception).when(chain).doFilter(request, response);

        try (MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getExceptionMsg(exception)).thenReturn("access denied");
            filter.doFilter(request, response, chain);
        }

        assertResponse(response, 403, "access denied");
    }

    @Test
    void nonAjaxAuthenticationExceptionIsRethrown() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain chain = mock(FilterChain.class);
        final InsufficientAuthenticationException exception = new InsufficientAuthenticationException("auth detail");
        doThrow(exception).when(chain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isSameAs(exception);
    }

    private MockHttpServletRequest ajaxRequest() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept", "application/json");
        return request;
    }

    private void assertResponse(final MockHttpServletResponse response, final int status, final String message) throws Exception {
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        final AjaxResponse body = objectMapper.readValue(response.getContentAsByteArray(), AjaxResponse.class);
        assertThat(body.getRslt()).isFalse();
        assertThat(body.getStatus()).isEqualTo(status);
        assertThat(body.getMessage()).isEqualTo(message);
    }
}
