package io.nicheblog.dreamdiary.infrastructure.log.interceptor;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.support.HttpRequestLogParamBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 요청 단위 로그(슬랙·DB) — {@link org.springframework.web.bind.annotation.RestController}는
 * {@link io.nicheblog.dreamdiary.infrastructure.log.aspect.LogControllerAspect}가 적재한 뒤 중복 발행을 건너뛴다.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class LogInterceptor implements HandlerInterceptor {

    private final ApplicationEventPublisherWrapper publisher;

    @Override
    public boolean preHandle(final HttpServletRequest request, final @NotNull HttpServletResponse response, final @NotNull Object handler) throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());
        if (handler instanceof HandlerMethod handlerMethod) {
            log.info(
                    "REQUEST_START method={} uri={} handler={} user={} ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    getHandlerSignature(handlerMethod),
                    getUsernameForLog(),
                    AuthUtils.getRemoteIpAddr()
            );
        }
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final @NotNull HttpServletResponse response, final @NotNull Object handler, final Exception ex) {

        final Long start = (Long) request.getAttribute("startTime");
        if (start == null) return;
        final long duration = System.currentTimeMillis() - start;

        if (!(handler instanceof HandlerMethod handlerMethod)) return;

        final String handlerSignature = getHandlerSignature(handlerMethod);
        final String username = getUsernameForLog();

        if (ex != null) {
            log.error(
                    "REQUEST_FAILED method={} uri={} handler={} status={} durationMs={} user={} ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    handlerSignature,
                    response.getStatus(),
                    duration,
                    username,
                    AuthUtils.getRemoteIpAddr(),
                    ex
            );
        } else if (response.getStatus() >= 400) {
            log.warn(
                    "REQUEST_END method={} uri={} handler={} status={} durationMs={} user={} ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    handlerSignature,
                    response.getStatus(),
                    duration,
                    username,
                    AuthUtils.getRemoteIpAddr()
            );
        } else {
            log.info(
                    "REQUEST_END method={} uri={} handler={} status={} durationMs={} user={} ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    handlerSignature,
                    response.getStatus(),
                    duration,
                    username,
                    AuthUtils.getRemoteIpAddr()
            );
        }

        final LogParam param = HttpRequestLogParamBuilder.build(request, response, handlerMethod, ex, duration, null);
        if (Boolean.TRUE.equals(request.getAttribute(HttpRequestLogParamBuilder.REST_ACTIVITY_LOGGED))) {
            return;
        }
        publisher.publishAsyncEvent(new LogEvent(this, param));
    }

    private String getHandlerSignature(final HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
    }

    private String getUsernameForLog() {
        final String username = AuthUtils.getLoginUsername();
        return username == null ? "anonymous" : username;
    }
}
