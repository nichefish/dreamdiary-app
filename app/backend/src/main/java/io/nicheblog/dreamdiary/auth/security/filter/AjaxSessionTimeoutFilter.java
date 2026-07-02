package io.nicheblog.dreamdiary.auth.security.filter;

import io.nicheblog.dreamdiary.auth.security.handler.SecurityErrorResponseWriter;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogAnonymousEvent;
import io.nicheblog.dreamdiary.infrastructure.log.handler.LogEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AjaxSessionTimeoutFilter
 * <pre>
 *  Ajax 인증만료시 클라이언트에서 식별 후 로그인 페이지 이동 처리.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class AjaxSessionTimeoutFilter
        implements Filter {

    private FilterConfig filterConfig;

    protected final ApplicationEventPublisher publisher;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    /**
     * Ajax 요청에 대하여 응답 설정 및 로깅 처리
     *
     * @see LogEventListener
     */
    @Override
    public void doFilter(
            final ServletRequest req,
            final ServletResponse res,
            final FilterChain chain
    ) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        try {
            chain.doFilter(request, response);
        } catch (final AuthenticationException e) {
            // (Ajax 요청에 대해서만 처리)
            if (HttpUtils.isAjaxRequest(request)) {
                securityErrorResponseWriter.write(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        MessageUtils.getMessage("auth.login-required")
                );
                // 로그 관련 처리
                final LogParam logParam = new LogParam(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.DEFAULT);
                publisher.publishEvent(new LogAnonymousEvent(this, logParam));
                return;
            }
            throw e;
        } catch (final AccessDeniedException e) {
            // (Ajax 요청에 대해서만 처리)
            if (HttpUtils.isAjaxRequest(request)) {
                securityErrorResponseWriter.write(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        MessageUtils.getExceptionMsg(e)
                );
                // 로그 관련 처리
                final LogParam logParam = new LogParam(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.DEFAULT);
                publisher.publishEvent(new LogAnonymousEvent(this, logParam));
                return;
            }
            throw e;
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}
