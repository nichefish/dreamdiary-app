package io.nicheblog.dreamdiary.auth.security.filter;

import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogAnonActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.util.HttpUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
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

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    /**
     * Ajax 요청에 대하여 응답 설정 및 로깅 처리
     *
     * @see LogActvtyEventListener
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
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);     // 401
                // 로그 관련 처리
                final LogActvtyParam logParam = new LogActvtyParam(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.DEFAULT);
                publisher.publishEvent(new LogAnonActvtyEvent(this, logParam));
            }
        } catch (final AccessDeniedException e) {
            // (Ajax 요청에 대해서만 처리)
            if (HttpUtils.isAjaxRequest(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);        // 403
                // 로그 관련 처리
                final LogActvtyParam logParam = new LogActvtyParam(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.DEFAULT);
                publisher.publishEvent(new LogAnonActvtyEvent(this, logParam));
            }
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}